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
    public Payment (Order order, String paymentKey, Double amount, String status) {
        this.order = order;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = status;
    }

    // 결제 성공시 update
    public void complete() {
        this.status = PaymentStatus.PAID.toString();
        this.paidAt = LocalDateTime.now();
    }

    // 결제 실패시 upate
    public void fail() {
        this.status = PaymentStatus.FAILED.toString();
    }
}
