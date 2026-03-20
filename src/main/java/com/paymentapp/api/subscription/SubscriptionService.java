package com.paymentapp.api.subscription;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberService;
import com.paymentapp.api.plan.PlanService;
import com.paymentapp.api.plan.entity.Plan;
import com.paymentapp.api.plan.PlanRepository;
import com.paymentapp.api.subscription.dto.ChangeSubscriptionPlanResponse;
import com.paymentapp.api.subscription.dto.CreateSubscriptionResponse;
import com.paymentapp.api.subscription.dto.SubscriptionResponse;
import com.paymentapp.api.subscription.dto.UpdateSubscriptionResponse;
import com.paymentapp.api.subscription.entity.*;
import com.paymentapp.core.exception.custom.PlanException;
import com.paymentapp.core.exception.custom.SubscriptionException;
import com.paymentapp.core.exception.errorcode.PlanErrorCode;
import com.paymentapp.core.exception.errorcode.SubscriptionErrorCode;
import com.paymentapp.core.portone.PortOneClient;
import com.paymentapp.core.portone.dto.PortOneBillingPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionPaymentMethodRepository paymentMethodRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionBillingRepository billingRepository;

    private final PlanService planService;
    private final MemberService memberService;

    private final PortOneClient portOneClient;

    /**
     * 구독 생성
     */
    @Transactional
    public CreateSubscriptionResponse createSubscription(
            Long memberId,
            String customerUid,
            String planId,
            String billingKey,
            BigDecimal amount) {
        boolean isAlreadySubscribed = subscriptionRepository.existsByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE);
        if (isAlreadySubscribed) {
            log.warn("다중 구독 시도 차단! memberId: {}", memberId);
             throw new SubscriptionException(SubscriptionErrorCode.ALREADY_SUBSCRIBED);
        }

        Member member = memberService.findById(memberId);
        Plan plan = planService.findByPlanId(planId);
        if(!plan.isActive()) {
            throw new PlanException(PlanErrorCode.PLAN_INACTIVE);
        }
        if (plan.getAmount().compareTo(amount) != 0) {
            throw new PlanException(PlanErrorCode.AMOUNT_MISMATCH);
        }



        // 빌링키 검증 (to 포트원)
        boolean isValidBillingKey = portOneClient.validateBillingKey(billingKey);
        if (!isValidBillingKey) {
            throw new SubscriptionException(SubscriptionErrorCode.INVALID_BILLING_KEY);
        }

        SubscriptionPaymentMethod paymentMethod = SubscriptionPaymentMethod.builder()
                .member(member)
                .customerUid(customerUid)
                .billingKey(billingKey)
                .pgProvider(PgProvider.TOSS_PAYMENTS)
                .isDefault(true)
                .status(PaymentMethodStatus.ACTIVE)
                .build();
        SubscriptionPaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);

        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = Subscription.builder()
                .member(member)
                .plan(plan)
                .paymentMethod(savedPaymentMethod)
                .amount(plan.getAmount())
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(now)
                .currentPeriodEnd(now.plusMonths(1))
                .build();
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // 결제
        String uniquePaymentId = "SUBSCRIPTION_PAY_" + savedSubscription.getId() + "_" + System.currentTimeMillis();
        PortOneBillingPaymentResponse paymentResult = portOneClient.payWithBillingKey(
                billingKey,
                plan.getAmount(),
                uniquePaymentId
        );

        // Billing을 DB에 기록
        if (paymentResult.isSuccess()) {
            SubscriptionBilling onSuccessBilling = SubscriptionBilling.builder()
                    .subscription(savedSubscription)
                    .amount(plan.getAmount())
                    .status(BillingStatus.COMPLETED)
                    .paymentId(uniquePaymentId)
                    .attemptedAt(now)
                    .errorMessage(null)
                    .build();
            billingRepository.save(onSuccessBilling);

        } else {
            savedSubscription.failPayment();
            SubscriptionBilling onFailureBilling = SubscriptionBilling.builder()
                    .subscription(savedSubscription)
                    .amount(plan.getAmount())
                    .status(BillingStatus.FAILED)
                    .paymentId(uniquePaymentId)
                    .attemptedAt(now)
                    .errorMessage(paymentResult.getErrorMessage())
                    .build();
            billingRepository.save(onFailureBilling);
            log.warn("결제 실패하였습니다.: {}", paymentResult.getErrorMessage());
            throw new SubscriptionException(SubscriptionErrorCode.FAILURE_PAYMENT);
        }

        return new CreateSubscriptionResponse(String.valueOf(savedSubscription.getId()));
    }

    // Q) 'paymentId' 를 포트원이 결제를 성공시키고 나서 발급해 주면 안되나? 왜 우리가 발급하지?
    // A)
    // =>  포트원에서 결제 ID를 발급하게되면 일시적으로 인터넷이 끊겼을때 우리는 포트원의 서버 응답을 못받게 된다.
    // 이때 한번 결제한 상황이면 중복체크를 하지못해 재결재가 되는 불상사가 일어날수있다


    /**
     * 내 구독 정보 조회
     */
    public SubscriptionResponse getMySubscription(Long memberId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdAndMemberId(subscriptionId, memberId)
                .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.ACTIVE_SUBSCRIPTION_NOT_FOUND));

        return SubscriptionResponse.from(subscription);
    }

    /**
     * 구독 해지
     */
    @Transactional
    public UpdateSubscriptionResponse cancelSubscription(Long memberId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdAndMemberId(subscriptionId, memberId)
                .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.CANCELABLE_SUBSCRIPTION_NOT_FOUND));
        subscription.cancel();

        return new UpdateSubscriptionResponse(true, String.valueOf(subscriptionId));
    }


    /**
     * 구독 플랜 변경
     */
    @Transactional
    public ChangeSubscriptionPlanResponse changePlan(Long userId, String subscriptionId, String newPlanId) {
        Long id = parseSubscriptionId(subscriptionId);

        Subscription subscription = subscriptionRepository.findByIdAndMemberId(id, userId)
                .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.ACTIVE_SUBSCRIPTION_NOT_FOUND));

        if (!subscription.isActive()) {
            throw new SubscriptionException(SubscriptionErrorCode.INVALID_SUBSCRIPTION_STATUS_MESSAGE);
        }

        Plan newPlan = planService.findByPlanId(newPlanId);

        if (!newPlan.isActive()) {
            throw new PlanException(PlanErrorCode.PLAN_INACTIVE);
        }

        if (subscription.getPlan().getPlanId().equals(newPlanId)) {
            throw new SubscriptionException(SubscriptionErrorCode.SAME_PLAN_NOT_ALLOWED);
        }

        if (subscription.getNextPlan() != null
                && subscription.getNextPlan().getPlanId().equals(newPlanId)) {
            throw new IllegalStateException("이미 동일한 플랜 변경이 예약되어 있습니다.");
        }

        subscription.reservePlanChange(newPlan);

        return new ChangeSubscriptionPlanResponse(
                true,
                String.valueOf(subscription.getId()),
                subscription.getPlan().getPlanId(),
                subscription.getNextPlan().getPlanId(),
                subscription.getStatus().name()
        );
    }

    private Long parseSubscriptionId(String subscriptionId) {
        try {
            return Long.parseLong(subscriptionId);
        } catch (NumberFormatException e) {
            throw new SubscriptionException(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID);
        }
    }
}