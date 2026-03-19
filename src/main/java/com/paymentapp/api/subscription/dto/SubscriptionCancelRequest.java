package com.paymentapp.api.subscription.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscriptionCancelRequest {

    private String reason;

}