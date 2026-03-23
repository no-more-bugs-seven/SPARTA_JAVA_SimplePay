package com.paymentapp.api.subscription.dto;

import java.math.BigDecimal;

public record CreateBillingResponse(
        boolean success,
        String billingId,
        String paymentId,
        BigDecimal amount,
        String status
) {}
