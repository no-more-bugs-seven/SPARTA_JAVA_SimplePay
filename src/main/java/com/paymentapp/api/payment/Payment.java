package com.paymentapp.api.payment;

import com.paymentapp.api.order.Order;
import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private String paymentKey;
    private Double amount;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    @Builder
    public Payment (Order order, Double amount) {
        this.order = order;
        this.amount = amount;
    }
}
