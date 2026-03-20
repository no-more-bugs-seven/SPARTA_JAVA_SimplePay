package com.paymentapp.api.order.dto;
/**
 * 주문 생성 요청 DTO
 */

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CreateOrderRequest {

    private List<OrderItemRequest> items;
    private BigDecimal usedPoints;

    @Getter
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}