package com.paymentapp.api.payment.dto;

import lombok.Builder;

@Builder
public record CreatePaymentResponse (
        String paymentId
) {
    public static CreatePaymentResponse of(String paymentId) {
        return CreatePaymentResponse.builder()
                .paymentId(paymentId)
                .build();
    }
}
