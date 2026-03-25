package com.paymentapp.api.order.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING,
    PAID,
    CANCELLED,
    REFUNDED
}