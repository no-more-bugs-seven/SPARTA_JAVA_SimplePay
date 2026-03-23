package com.paymentapp.api.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BillingHistoryResponse(
        String billingId,
        String paymentId,
        BigDecimal amount,
        String status,
        LocalDateTime attemptedAt,
        String errorMessage
) {}
