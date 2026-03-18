package com.paymentapp.api.payment.dto;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        Long orderId,
        BigDecimal totalAmount
) {
}
