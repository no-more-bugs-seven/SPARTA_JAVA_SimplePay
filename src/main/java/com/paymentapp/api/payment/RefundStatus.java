package com.paymentapp.api.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundStatus {
    REQUESTED("환불요청"),
    COMPLETED("환불완료"),
    FAILED("환불실패");

    private final String description;
}
