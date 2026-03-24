package com.paymentapp.api.plan;

import com.paymentapp.api.plan.dto.PlanResponse;
import com.paymentapp.api.plan.entity.BillingCycle;
import com.paymentapp.api.plan.entity.Plan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    @Test
    @DisplayName("TC_SUB_001_SUCCESS - 구독 플랜 목록 조회 성공")
    void getPlans_success() {
        // given
        List<Plan> plans = List.of(
                new Plan("NOOB", "입문자", new BigDecimal("9900"), BillingCycle.MONTHLY, true),
                new Plan("BROKER", "중개인", new BigDecimal("19900"), BillingCycle.MONTHLY, true),
                new Plan("BLACK_HAND", "검은손", new BigDecimal("29900"), BillingCycle.MONTHLY, true)
        );

        given(planRepository.findAllByActiveTrueOrderByIdAsc()).willReturn(plans);

        // when
        List<PlanResponse> result = planService.getPlans();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).planId()).isEqualTo("NOOB");
        assertThat(result.get(1).planId()).isEqualTo("BROKER");
        assertThat(result.get(2).planId()).isEqualTo("BLACK_HAND");
    }

    @Test
    @DisplayName("TC_SUB_001_FAIL - 활성 플랜이 없으면 빈 목록 반환")
    void getPlans_fail_emptyList() {
        // given
        given(planRepository.findAllByActiveTrueOrderByIdAsc()).willReturn(Collections.emptyList());

        // when
        List<PlanResponse> result = planService.getPlans();

        // then
        assertThat(result).isEmpty();
    }
}