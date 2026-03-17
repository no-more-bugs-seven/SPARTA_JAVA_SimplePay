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
    private Long orderId;
    private BigDecimal totalAmount;
    private BigDecimal usedPoints;
    private String status;
    private LocalDateTime createdAt;
}