package com.paymentapp.api.payment.dto;

import lombok.Builder;

@Builder
public record CancelPaymentResponse (
        boolean success,
        String paymentId,
        String status
) {
    public static CancelPaymentResponse of(boolean success, String paymentId, String status) {
        return CancelPaymentResponse.builder()
                .success(success)
                .paymentId(paymentId)
                .status(status)
                .build();
    }
}
