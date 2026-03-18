package com.paymentapp.api.subscription;


import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberService;
import com.paymentapp.api.subscription.dto.CustomerUidResponse;
import com.paymentapp.api.subscription.dto.MySubscriptionResponse;
import com.paymentapp.api.subscription.dto.SubscriptionCreateRequest;
import com.paymentapp.api.subscription.dto.SubscriptionCreateResponse;
import com.paymentapp.api.subscription.entity.Subscription;
import com.paymentapp.api.subscription.entity.SubscriptionStatus;
import com.paymentapp.core.exception.custom.SubscriptionException;
import com.paymentapp.core.exception.errorcode.SubscriptionErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정해 속도를 높입니다.
public class SubscriptionService {

    private final MemberService memberService;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPaymentMethodRepository paymentMethodRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionBillingRepository billingRepository;

    /**
     * CustomerUid 조회
     */
    public CustomerUidResponse generateCustomerUid(Long memberId) {
        Member member = memberService.findById(memberId);

        String customerUid = "CUST-" + memberId;

        return new CustomerUidResponse(customerUid);
    }

    /**
     *  구독
     */
    @Transactional
    public SubscriptionCreateResponse subscribe(Long memberId, SubscriptionCreateRequest request) {

        return null;
    }

    /**
     * 내 구독 정보 조회
     */
    @Transactional
    public MySubscriptionResponse getMySubscription(Long memberId) {
        Subscription subscription = subscriptionRepository.findByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.CANCELABLE_SUBSCRIPTION_NOT_FOUND));

        return MySubscriptionResponse.from(subscription); // DTO의 from 메서드로 예쁘게 변환!
    }

    /**
     * 구독 해지
     */
    @Transactional
    public void cancelSubscription(Long memberId) {
        Subscription subscription = subscriptionRepository.findByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.CANCELABLE_SUBSCRIPTION_NOT_FOUND));
        subscription.cancel();
    }
}
