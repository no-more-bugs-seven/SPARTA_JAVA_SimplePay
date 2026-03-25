package com.paymentapp.api.payment.entity;

import com.paymentapp.api.payment.enums.RefundStatus;
import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refunds")
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    private BigDecimal amount;
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    private LocalDateTime refundedAt;

    @Builder
    public Refund (Payment payment, BigDecimal amount, String reason, RefundStatus status,  LocalDateTime refundedAt) {
        this.payment = payment;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.refundedAt = refundedAt;
    }
}
