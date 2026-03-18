package com.paymentapp.api.subscription.dto;

import com.paymentapp.api.subscription.entity.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MySubscriptionResponse {

    private String planName;
    private Integer price;
    private String status;
    private LocalDateTime nextPaymentDate;
    private String pgProvider;

    public static MySubscriptionResponse from(Subscription subscription) {
        return new MySubscriptionResponse(
                subscription.getPlan().getName(),
                subscription.getPlan().getPrice(),
                subscription.getStatus().toString(),
                subscription.getCurrentPeriodEnd(),
                subscription.getSubscriptionPaymentMethod().getPgProvider()
        );
    }
}