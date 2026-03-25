package com.paymentapp.api.subscription;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberService;
import com.paymentapp.api.plan.PlanService;
import com.paymentapp.api.plan.entity.Plan;
import com.paymentapp.api.subscription.dto.ChangeSubscriptionPlanResponse;
import com.paymentapp.api.subscription.dto.CreateSubscriptionResponse;
import com.paymentapp.api.subscription.dto.SubscriptionResponse;
import com.paymentapp.api.subscription.dto.UpdateSubscriptionResponse;
import com.paymentapp.api.subscription.entity.*;
import com.paymentapp.api.subscription.enums.SubscriptionStatus;
import com.paymentapp.core.exception.custom.SubscriptionException;
import com.paymentapp.api.subscription.enums.SubscriptionStatus;
import com.paymentapp.api.subscription.exception.SubscriptionException;
import com.paymentapp.core.portone.PortOneClient;
import com.paymentapp.core.portone.dto.PortOneBillingPaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionPaymentMethodRepository paymentMethodRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionBillingRepository billingRepository;

    @Mock
    private PlanService planService;

    @Mock
    private MemberService memberService;

    @Mock
    private PortOneClient portOneClient;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    @DisplayName("TC_SUB_002_SUCCESS - 구독 생성 성공")
    void createSubscription_success() {
        // given
        Long memberId = 1L;

        Member member = org.mockito.Mockito.mock(Member.class);

        Plan plan = org.mockito.Mockito.mock(Plan.class);
        given(plan.isActive()).willReturn(true);
        given(plan.getAmount()).willReturn(new BigDecimal("19900"));

        SubscriptionPaymentMethod savedPaymentMethod = org.mockito.Mockito.mock(SubscriptionPaymentMethod.class);

        PortOneBillingPaymentResponse paymentResponse = org.mockito.Mockito.mock(PortOneBillingPaymentResponse.class);
        given(paymentResponse.isSuccess()).willReturn(true);

        given(subscriptionRepository.existsByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE))
                .willReturn(false);
        given(memberService.findById(memberId)).willReturn(member);
        given(planService.findByPlanId("BROKER")).willReturn(plan);
        given(portOneClient.validateBillingKey("billing-key-test")).willReturn(true);
        given(paymentMethodRepository.save(any(SubscriptionPaymentMethod.class))).willReturn(savedPaymentMethod);
        given(portOneClient.payWithBillingKey(anyString(), eq(new BigDecimal("19900")), anyString()))
                .willReturn(paymentResponse);

        Subscription savedSubscription = Subscription.builder()
                .member(member)
                .plan(plan)
                .paymentMethod(savedPaymentMethod)
                .amount(new BigDecimal("19900"))
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .build();
        ReflectionTestUtils.setField(savedSubscription, "id", 1L);

        given(subscriptionRepository.save(any(Subscription.class))).willReturn(savedSubscription);

        // when
        CreateSubscriptionResponse response = subscriptionService.createSubscription(
                memberId,
                "CUST-1",
                "BROKER",
                "billing-key-test",
                new BigDecimal("19900")
        );

        // then
        assertThat(response).isNotNull();
        verify(paymentMethodRepository).save(any(SubscriptionPaymentMethod.class));
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(billingRepository).save(any(SubscriptionBilling.class));
    }

    @Test
    @DisplayName("TC_SUB_003_FAIL - 진행 중인 ACTIVE 구독이 있으면 중복 구독 생성 실패")
    void createSubscription_fail_whenAlreadySubscribed() {
        // given
        Long memberId = 1L;

        given(subscriptionRepository.existsByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> subscriptionService.createSubscription(
                memberId,
                "CUST-1",
                "BROKER",
                "billing-key-test",
                new BigDecimal("19900")
        )).isInstanceOf(SubscriptionException.class);
    }

    @Test
    @DisplayName("TC_SUB_004_SUCCESS - 내 구독 단건 조회 성공")
    void getMySubscription_success() {
        // given
        Long memberId = 1L;
        Long subscriptionId = 10L;

        Plan plan = org.mockito.Mockito.mock(Plan.class);
        SubscriptionPaymentMethod paymentMethod = org.mockito.Mockito.mock(SubscriptionPaymentMethod.class);

        Subscription subscription = Subscription.builder()
                .member(org.mockito.Mockito.mock(Member.class))
                .plan(plan)
                .paymentMethod(paymentMethod)
                .amount(new BigDecimal("19900"))
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .build();
        ReflectionTestUtils.setField(subscription, "id", subscriptionId);

        given(subscriptionRepository.findByIdAndMemberId(subscriptionId, memberId))
                .willReturn(Optional.of(subscription));

        // when
        SubscriptionResponse response = subscriptionService.getMySubscription(memberId, subscriptionId);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("TC_SUB_005_FAIL - 존재하지 않거나 본인 것이 아닌 구독 조회 실패")
    void getMySubscription_fail_whenNotFound() {
        // given
        Long memberId = 1L;
        Long subscriptionId = 10L;

        given(subscriptionRepository.findByIdAndMemberId(subscriptionId, memberId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionService.getMySubscription(memberId, subscriptionId))
                .isInstanceOf(SubscriptionException.class);
    }

    @Test
    @DisplayName("TC_SUB_006_SUCCESS - ACTIVE 구독 해지 성공")
    void cancelSubscription_success() {
        // given
        Long memberId = 1L;
        Long subscriptionId = 10L;

        Subscription subscription = Subscription.builder()
                .member(org.mockito.Mockito.mock(Member.class))
                .plan(org.mockito.Mockito.mock(Plan.class))
                .paymentMethod(org.mockito.Mockito.mock(SubscriptionPaymentMethod.class))
                .amount(new BigDecimal("9900"))
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .build();
        ReflectionTestUtils.setField(subscription, "id", subscriptionId);

        given(subscriptionRepository.findByIdAndMemberId(subscriptionId, memberId))
                .willReturn(Optional.of(subscription));

        // when
        UpdateSubscriptionResponse response =
                subscriptionService.cancelSubscription(memberId, subscriptionId);

        // then
        assertThat(response).isNotNull();
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(subscription.getCurrentPeriodEnd())
                .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("TC_SUB_007_FAIL - PAST_DUE 상태 구독은 해지 실패")
    void cancelSubscription_fail_whenPastDue() {
        // given
        Long memberId = 1L;
        Long subscriptionId = 10L;

        Subscription subscription = Subscription.builder()
                .member(org.mockito.Mockito.mock(Member.class))
                .plan(org.mockito.Mockito.mock(Plan.class))
                .paymentMethod(org.mockito.Mockito.mock(SubscriptionPaymentMethod.class))
                .amount(new BigDecimal("9900"))
                .status(SubscriptionStatus.PAST_DUE)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .build();
        ReflectionTestUtils.setField(subscription, "id", subscriptionId);

        given(subscriptionRepository.findByIdAndMemberId(subscriptionId, memberId))
                .willReturn(Optional.of(subscription));

        // when & then
        assertThatThrownBy(() -> subscriptionService.cancelSubscription(memberId, subscriptionId))
                .isInstanceOf(SubscriptionException.class);
    }

    @Test
    @DisplayName("TC_SUB_008_SUCCESS - ACTIVE 상태에서 플랜 변경 성공")
    void changePlan_success() {
        // given
        Long memberId = 1L;
        String subscriptionId = "10";

        Plan currentPlan = org.mockito.Mockito.mock(Plan.class);
        given(currentPlan.getPlanId()).willReturn("NOOB");

        Plan newPlan = org.mockito.Mockito.mock(Plan.class);
        given(newPlan.getPlanId()).willReturn("BROKER");
        given(newPlan.isActive()).willReturn(true);
        given(newPlan.getAmount()).willReturn(new BigDecimal("19900"));

        Subscription subscription = Subscription.builder()
                .member(org.mockito.Mockito.mock(Member.class))
                .plan(currentPlan)
                .paymentMethod(org.mockito.Mockito.mock(SubscriptionPaymentMethod.class))
                .amount(new BigDecimal("9900"))
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .build();
        ReflectionTestUtils.setField(subscription, "id", 10L);

        given(subscriptionRepository.findByIdAndMemberId(10L, memberId))
                .willReturn(Optional.of(subscription));
        given(planService.findByPlanId("BROKER")).willReturn(newPlan);

        // when
        ChangeSubscriptionPlanResponse response =
                subscriptionService.changePlan(memberId, subscriptionId, "BROKER");

        // then
        assertThat(response).isNotNull();
        assertThat(subscription.getPlan()).isEqualTo(newPlan);
    }

    @Test
    @DisplayName("TC_SUB_011_FAIL - ACTIVE 아닌 상태에서는 플랜 변경 실패")
    void changePlan_fail_whenNotActive() {
        // given
        Long memberId = 1L;
        String subscriptionId = "10";

        Plan currentPlan = org.mockito.Mockito.mock(Plan.class);

        Subscription subscription = Subscription.builder()
                .member(org.mockito.Mockito.mock(Member.class))
                .plan(currentPlan)
                .paymentMethod(org.mockito.Mockito.mock(SubscriptionPaymentMethod.class))
                .amount(new BigDecimal("9900"))
                .status(SubscriptionStatus.CANCELLED)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .build();
        ReflectionTestUtils.setField(subscription, "id", 10L);

        given(subscriptionRepository.findByIdAndMemberId(10L, memberId))
                .willReturn(Optional.of(subscription));

        // when & then
        assertThatThrownBy(() -> subscriptionService.changePlan(memberId, subscriptionId, "BROKER"))
                .isInstanceOf(SubscriptionException.class);
    }
}