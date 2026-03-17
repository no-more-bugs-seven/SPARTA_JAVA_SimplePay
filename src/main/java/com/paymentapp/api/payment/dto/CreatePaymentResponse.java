package com.paymentapp.api.payment.dto;

import lombok.Builder;

@Builder
public record CreatePaymentResponse (
        boolean success,
        String paymentId,
        String status
) {
    public static CreatePaymentResponse of(boolean success, String paymentId, String status) {
        return CreatePaymentResponse.builder()
                .success(success)
                .paymentId(paymentId)
                .status(status)
                .build();
    }
}
