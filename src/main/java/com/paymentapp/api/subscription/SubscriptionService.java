package com.paymentapp.api.subscription;

import com.paymentapp.api.plan.PlanRepository;
import com.paymentapp.api.subscription.dto.ChangeSubscriptionPlanResponse;
import com.paymentapp.api.subscription.dto.CreateSubscriptionResponse;
import com.paymentapp.api.subscription.dto.SubscriptionResponse;
import com.paymentapp.api.subscription.dto.UpdateSubscriptionResponse;
import com.paymentapp.api.plan.entity.Plan;
import com.paymentapp.api.subscription.entity.Subscription;
import com.paymentapp.api.subscription.entity.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private static final List<SubscriptionStatus> IN_PROGRESS_STATUSES =
            List.of(
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.SUSPENDED,
                    SubscriptionStatus.CANCELLED
            );

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    @Transactional
    public CreateSubscriptionResponse create(
            Long userId,
            String customerUid,
            String planId,
            String billingKey,
            BigDecimal amount
    ) {
        validateNoInProgressSubscription(userId);

        Plan plan = planRepository.findByPlanId(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플랜입니다."));

        if (!plan.isActive()) {
            throw new IllegalStateException("비활성화된 플랜은 구독할 수 없습니다.");
        }

        if (plan.getAmount().compareTo(amount) != 0) {
            throw new IllegalArgumentException("요청 금액이 플랜 금액과 일치하지 않습니다.");
        }

        if (billingKey == null || billingKey.isBlank()) {
            throw new IllegalArgumentException("billingKey는 필수입니다.");
        }


        String paymentMethodId = null;

        Subscription subscription = Subscription.create(
                userId,
                customerUid,
                paymentMethodId,
                plan,
                amount
        );

        Subscription saved = subscriptionRepository.save(subscription);
        return new CreateSubscriptionResponse(String.valueOf(saved.getId()));
    }

    public SubscriptionResponse getSubscription(Long userId, String subscriptionId) {
        Long id = parseSubscriptionId(subscriptionId);

        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("구독이 존재하지 않거나 조회 권한이 없습니다."));

        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public UpdateSubscriptionResponse cancel(Long userId, String subscriptionId) {
        Long id = parseSubscriptionId(subscriptionId);

        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("구독이 존재하지 않거나 해지 권한이 없습니다."));

        subscription.cancel();

        return new UpdateSubscriptionResponse(
                true,
                String.valueOf(subscription.getId()),
                subscription.getStatus().name()
        );
    }

    @Transactional
    public ChangeSubscriptionPlanResponse changePlan(Long userId, String subscriptionId, String newPlanId) {
        Long id = parseSubscriptionId(subscriptionId);

        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("구독이 존재하지 않거나 변경 권한이 없습니다."));

        if (!subscription.isActive()) {
            throw new IllegalStateException("ACTIVE 상태의 구독만 플랜 변경이 가능합니다.");
        }

        Plan newPlan = planRepository.findByPlanId(newPlanId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플랜입니다."));

        if (!newPlan.isActive()) {
            throw new IllegalStateException("비활성화된 플랜으로는 변경할 수 없습니다.");
        }

        if (subscription.getPlan().getPlanId().equals(newPlanId)) {
            throw new IllegalStateException("현재 이용 중인 플랜과 동일한 플랜으로는 변경할 수 없습니다.");
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

    private void validateNoInProgressSubscription(Long userId) {
        boolean exists = subscriptionRepository
                .findFirstByUserIdAndStatusInAndCurrentPeriodEndAfterOrderByIdDesc(
                        userId,
                        IN_PROGRESS_STATUSES,
                        LocalDateTime.now()
                )
                .isPresent();

        if (exists) {
            throw new IllegalStateException("이미 진행 중인 구독이 존재합니다.");
        }
    }

    private Long parseSubscriptionId(String subscriptionId) {
        try {
            return Long.parseLong(subscriptionId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("subscriptionId 형식이 올바르지 않습니다.");
        }
    }
}