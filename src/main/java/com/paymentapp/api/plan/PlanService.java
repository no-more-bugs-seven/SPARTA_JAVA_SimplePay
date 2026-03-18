package com.paymentapp.api.plan;

import com.paymentapp.api.plan.dto.PlanResponse;
import com.paymentapp.api.plan.entity.Plan;
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
}