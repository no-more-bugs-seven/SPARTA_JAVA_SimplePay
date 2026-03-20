package com.paymentapp.api.subscription.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateSubscriptionRequest(
        @NotBlank(message = "customerUid 는 필수입니다.")
        String customerUid,
        @NotBlank(message = "planId 는 필수입니다.")
        String planId,
        @NotBlank(message = "billingKey 는 필수입니다.")
        @NotBlank String billingKey,
        @NotNull @DecimalMin("0.0") BigDecimal amount
) {
}