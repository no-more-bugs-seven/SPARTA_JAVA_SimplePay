package com.paymentapp.api.subscription.dto;

public record ChangeSubscriptionPlanResponse(
        boolean success,
        String subscriptionId,
        String currentPlanId,
        String nextPlanId,
        String status
) {
}