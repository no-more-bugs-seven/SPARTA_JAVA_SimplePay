package com.paymentapp.api.subscription.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_billings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingStatus status;

    @Column(unique = true)
    private String paymentId;

    @Column(nullable = false)
    private LocalDateTime attemptedAt;

    @Column()
    private String errorMessage;

    @Builder
    public SubscriptionBilling(
            Subscription subscription,
            BigDecimal amount,
            BillingStatus status,
            String paymentId,
            LocalDateTime attemptedAt,
            String errorMessage
    ) {
        this.subscription = subscription;
        this.amount = amount;
        this.status = status;
        this.paymentId = paymentId;
        this.attemptedAt = attemptedAt;
        this.errorMessage = errorMessage;
    }
}