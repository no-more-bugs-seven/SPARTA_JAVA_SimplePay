package com.paymentapp.core.constant;

// points 컬럼은 양수로 저장
// - 잔액 증가: EARNED, RECOVERED
// - 잔액 감소: SPENT, CANCELED, EXPIRED, ADMIN_ADJUST
// balance = Σ(EARNED + RECOVERED) - Σ(SPENT + CANCELED + EXPIRED + ADMIN_ADJUST)

public enum PointTransactionType {
    EARNED,
    SPENT,
    RECOVERED,
    CANCELED,
    EXPIRED,
    ADMIN_ADJUST
}