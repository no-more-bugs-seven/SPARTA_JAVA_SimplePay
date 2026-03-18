package com.paymentapp.api.order.dto;

/**
 * 주문 단건 DTO
 */

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderDetailResponse {

    private Long orderId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private BigDecimal usedPoints;
    private String status;
    private LocalDateTime createdAt;

    private List<OrderItemDto> items;

    @Getter
    @Builder
    public static class OrderItemDto {
        private String productName;
        private BigDecimal productPrice;
        private Integer quantity;
    }
}
