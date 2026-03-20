package com.paymentapp.api.point.dto;

import com.paymentapp.api.point.PointTransaction;
import com.paymentapp.core.constant.PointTransactionType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointTransactionResponse(
        Long id,
        Long orderId,
        BigDecimal points,
        PointTransactionType transactionType,
        LocalDateTime expiryAt,
        LocalDateTime createdAt
) {
    public static PointTransactionResponse from(PointTransaction transaction) {
        return new PointTransactionResponse(
                transaction.getId(),
                transaction.getOrder() != null ? transaction.getOrder().getId() : null,
                transaction.getPoints(),
                transaction.getTransactionType(),
                transaction.getExpiredAt(),
                transaction.getCreatedAt()
        );
    }
}