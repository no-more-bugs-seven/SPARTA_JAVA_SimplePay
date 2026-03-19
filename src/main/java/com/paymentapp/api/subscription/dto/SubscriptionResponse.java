package com.paymentapp.api.subscription.dto;

import com.paymentapp.api.subscription.entity.Subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        String subscriptionId,
        String customerUid,
        String planId,
        String paymentMethodId,
        String status,
        BigDecimal amount,
        LocalDateTime currentPeriodEnd
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                String.valueOf(subscription.getId()),
                subscription.getCustomerUid(),
                subscription.getPlan().getPlanId(),
                subscription.getPaymentMethodId(),
                subscription.getStatus().name(),
                subscription.getAmount(),
                subscription.getCurrentPeriodEnd()
        );
    }
}