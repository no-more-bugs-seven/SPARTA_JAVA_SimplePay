package com.paymentapp.api.order;

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