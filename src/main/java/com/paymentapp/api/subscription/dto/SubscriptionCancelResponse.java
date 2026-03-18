package com.paymentapp.api.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubscriptionCancelResponse {

    private String status;
    private String message;

}