package com.paymentapp.api.plan;


import com.paymentapp.api.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findAllByActiveTrueOrderByIdAsc();

    Optional<Plan> findByPlanId(String planId);
}