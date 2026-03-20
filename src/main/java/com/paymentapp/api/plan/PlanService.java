package com.paymentapp.api.plan;

import com.paymentapp.api.plan.dto.PlanResponse;
import com.paymentapp.api.plan.entity.Plan;
import com.paymentapp.core.exception.custom.MemberException;
import com.paymentapp.core.exception.custom.PlanException;
import com.paymentapp.core.exception.errorcode.MemberErrorCode;
import com.paymentapp.core.exception.errorcode.PlanErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {

    private final PlanRepository planRepository;

    public List<PlanResponse> getPlans() {
        List<Plan> plans = planRepository.findAllByActiveTrueOrderByIdAsc();
        return plans.stream()
                .map(PlanResponse::from)
                .toList();
    }

    /**
     * 플랜 객체 가져오기
     */
    public Plan findByPlanId(String planId) {
        return planRepository.findByPlanId(planId)
                .orElseThrow(() -> new PlanException(PlanErrorCode.PLAN_NOT_FOUND));
    }
}