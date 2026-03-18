package com.paymentapp.api.plan.dto;

import com.paymentapp.api.plan.entity.Plan;

import java.math.BigDecimal;

public record PlanResponse(
        String planId,
        String name,
        BigDecimal amount,
        String billingCycle
) {
    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getPlanId(),
                plan.getName(),
                plan.getAmount(),
                plan.getBillingCycle().name()
        );
    }
}