package com.paymentapp.api.subscription.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeSubscriptionPlanRequest(
        @NotBlank String planId
) {
}