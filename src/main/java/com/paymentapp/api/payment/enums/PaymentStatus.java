package com.paymentapp.api.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("결제대기"),
    PAID("결제완료"),
    FAILED("결제실패"),
    REFUNDED("환불완료");

    private final String description;
}
