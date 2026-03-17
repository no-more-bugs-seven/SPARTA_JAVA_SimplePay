package com.paymentapp.api.order.dto;

/**
 * 주문 생성 응답 DTO
 */

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderCreateResponse {

    private Long orderId;
    private BigDecimal totalAmount;
    private String orderNumber;
}