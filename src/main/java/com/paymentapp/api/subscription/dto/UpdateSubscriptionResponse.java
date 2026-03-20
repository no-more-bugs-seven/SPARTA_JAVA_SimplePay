package com.paymentapp.api.subscription.dto;

public record UpdateSubscriptionResponse(
        boolean success,
        String subscriptionId
) {
}