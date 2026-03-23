package com.paymentapp.api.subscription.entity;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.plan.entity.Plan;
import com.paymentapp.core.entity.BaseEntity;
import com.paymentapp.core.exception.custom.SubscriptionException;
import com.paymentapp.core.exception.errorcode.SubscriptionErrorCode;
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
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_plan_id")
    private Plan nextPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_payment_methods_id", nullable = false)
    private SubscriptionPaymentMethod paymentMethod;

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

    private LocalDateTime nextPaymentAt; // 스케줄러가 결제 시점을 판단할 기준!

    @Builder
    public Subscription(
            Member member,
            Plan plan,
            SubscriptionPaymentMethod paymentMethod,
            BigDecimal amount,
            SubscriptionStatus status,
            LocalDateTime currentPeriodStart,
            LocalDateTime currentPeriodEnd,
            LocalDateTime nextPaymentAt
    ) {
        this.member = member;
        this.plan = plan;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status != null ? status : SubscriptionStatus.ACTIVE; // 기본값 셋팅
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.nextPaymentAt = nextPaymentAt;
    }

    public void failPayment() {
        if (this.status == SubscriptionStatus.CANCELLED) {
            return;
        }

        this.status = SubscriptionStatus.PAST_DUE;
    }

    public void reservePlanChange(Plan newPlan) {
        this.nextPlan = newPlan;
    }

    public void cancel() {
        if (this.status == SubscriptionStatus.CANCELLED) {
            return;
        }

        if (this.status == SubscriptionStatus.PAST_DUE) {
            throw new SubscriptionException(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_ENDED);
        }

        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE;
    }

    /**
     * [추가] 정기 결제 성공 시 구독 정보 갱신
     */
    public void renew(LocalDateTime start, LocalDateTime end, Plan confirmedPlan) {
        this.currentPeriodStart = start;
        this.currentPeriodEnd = end;
        this.nextPaymentAt = end; // 다음 결제일은 이번 주기가 끝나는 시점!
        this.plan = confirmedPlan; // 예약된 플랜이 있었다면 여기서 교체
        this.nextPlan = null;      // 예약 초기화
        this.status = SubscriptionStatus.ACTIVE;
    }
}