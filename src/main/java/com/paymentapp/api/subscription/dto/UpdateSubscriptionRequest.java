package com.paymentapp.api.subscription.dto;

public record UpdateSubscriptionRequest(
        String action,
        String reason,
        String planId
) {
}