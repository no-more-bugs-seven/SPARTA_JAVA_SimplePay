package com.paymentapp.api.payment.dto;

import lombok.Builder;

@Builder
public record CreatePaymentResponse (
        Long paymentId
) {
    public static CreatePaymentResponse of(Long paymentId) {
        return CreatePaymentResponse.builder()
                .paymentId(paymentId)
                .build();
    }
}
