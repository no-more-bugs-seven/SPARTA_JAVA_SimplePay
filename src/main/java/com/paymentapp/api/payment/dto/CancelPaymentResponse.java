package com.paymentapp.api.payment.dto;

import lombok.Builder;

@Builder
public record CancelPaymentResponse (
        boolean success,
        String orderId,
        String status
) {
    public static CancelPaymentResponse of(boolean success, String orderId, String status) {
        return CancelPaymentResponse.builder()
                .success(success)
                .orderId(orderId)
                .status(status)
                .build();
    }
}
