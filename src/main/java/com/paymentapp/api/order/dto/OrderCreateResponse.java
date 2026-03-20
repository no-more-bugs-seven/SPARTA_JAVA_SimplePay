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

    private String orderId;
    private BigDecimal totalAmount;
    private BigDecimal usedPoints;    // 사용한 포인트
    private BigDecimal finalAmount;   // 포인트 차감 후 실결제 금액
    private String orderNumber;
}