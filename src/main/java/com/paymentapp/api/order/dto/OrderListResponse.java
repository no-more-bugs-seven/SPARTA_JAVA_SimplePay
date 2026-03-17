package com.paymentapp.api.order.dto;

/**
 * 주문 목록 DTO
 */

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderListResponse {

    private String orderNumber;
    private String orderId;
    private BigDecimal totalAmount;
    private BigDecimal usedPoints;
    private BigDecimal finalAmount;
    private BigDecimal earnedPoints;
    private String currency;
    private String status;
    private String createdAt;
}