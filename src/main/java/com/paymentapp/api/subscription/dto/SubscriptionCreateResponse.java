package com.paymentapp.api.subscription.dto;

import com.paymentapp.api.subscription.entity.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SubscriptionCreateResponse {

    private Long subscriptionId;
    private String status;
    private LocalDateTime nextPaymentDate;

    public static SubscriptionCreateResponse from(Subscription subscription) {
        return new SubscriptionCreateResponse(
                subscription.getId(),
                subscription.getStatus().toString(),
                subscription.getCurrentPeriodEnd()
        );
    }
}
