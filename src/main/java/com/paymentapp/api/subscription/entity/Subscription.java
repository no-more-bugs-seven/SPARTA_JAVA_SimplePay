package com.paymentapp.api.subscription.entity;

import com.paymentapp.api.plan.entity.Plan;
import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String customerUid;

    @Column(length = 100)
    private String paymentMethodId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_plan_id")
    private Plan nextPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_payment_methods_id", nullable = false)
    private SubscriptionPaymentMethod subscriptionPaymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(nullable = false)
    private LocalDateTime currentPeriodEnd;

    private LocalDateTime cancelledAt;

    private Subscription(
            Long userId,
            String customerUid,
            String paymentMethodId,
            Plan plan,
            BigDecimal amount,
            LocalDateTime currentPeriodStart,
            LocalDateTime currentPeriodEnd
    ) {
        this.userId = userId;
        this.customerUid = customerUid;
        this.paymentMethodId = paymentMethodId;
        this.plan = plan;
        this.amount = amount;
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public static Subscription create(
            Long userId,
            String customerUid,
            String paymentMethodId,
            Plan plan,
            BigDecimal amount
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Subscription(
                userId,
                customerUid,
                paymentMethodId,
                plan,
                amount,
                now,
                now.plusMonths(1)
        );
    }

    public void reservePlanChange(Plan newPlan) {
        this.nextPlan = newPlan;
    }

    public void cancel() {
        if (this.status == SubscriptionStatus.CANCELLED) {
            return;
        }

        if (this.status == SubscriptionStatus.EXPIRED) {
            throw new IllegalStateException("이미 종료된 구독은 해지할 수 없습니다.");
        }

        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE;
    }

    public boolean isInProgress() {
        return (this.status == SubscriptionStatus.ACTIVE
                || this.status == SubscriptionStatus.SUSPENDED
                || this.status == SubscriptionStatus.CANCELLED)
                && this.currentPeriodEnd.isAfter(LocalDateTime.now());
    }

    public void linkPaymentMethod(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public void extendSubscription() {
        this.currentPeriodStart = this.currentPeriodEnd;
        this.currentPeriodEnd = this.currentPeriodEnd.plusMonths(1);
    }
}