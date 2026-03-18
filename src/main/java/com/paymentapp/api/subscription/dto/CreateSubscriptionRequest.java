package com.paymentapp.api.subscription.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateSubscriptionRequest(
        @NotBlank String customerUid,
        @NotBlank String planId,
        @NotBlank String billingKey,
        @NotNull @DecimalMin("0.0") BigDecimal amount
) {
}