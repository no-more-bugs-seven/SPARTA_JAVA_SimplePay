package com.paymentapp.api.order.dto;

/**
 * 주문 단건 DTO
 */

import com.paymentapp.api.point.dto.PointTransactionResponse;
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
    private BigDecimal finalAmount;
    private String status;
    private LocalDateTime createdAt;

    private List<OrderItemDto> items;
    private PaymentDto payment;
    private List<PointTransactionResponse> pointTransactions;

    @Getter
    @Builder
    public static class OrderItemDto {
        private String productName;
        private BigDecimal productPrice;
        private Integer quantity;
    }

    @Getter
    @Builder
    public static class PaymentDto {
        private String paymentKey;
        private BigDecimal amount;
        private String status;
        private LocalDateTime paidAt;
    }
}
