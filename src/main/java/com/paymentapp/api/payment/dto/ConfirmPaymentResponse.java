package com.paymentapp.api.payment.dto;

import lombok.Builder;

@Builder
public record ConfirmPaymentResponse (
        boolean success,
        String orderId,
        String status
) {
    public static ConfirmPaymentResponse of(boolean success, String orderId, String status) {
        return ConfirmPaymentResponse.builder()
                .success(success)
                .orderId(orderId)
                .status(status)
                .build();
    }
}