package com.paymentapp.api.payment.dto;

public record CreatePaymentRequest(
        Long orderId,
        Double totalAmount
) {
}
