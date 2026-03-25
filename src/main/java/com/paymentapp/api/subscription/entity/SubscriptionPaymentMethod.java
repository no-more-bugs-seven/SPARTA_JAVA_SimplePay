package com.paymentapp.api.subscription.entity;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.subscription.enums.PaymentMethodStatus;
import com.paymentapp.api.subscription.enums.PgProvider;
import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscription_payment_methods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionPaymentMethod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String customerUid;

    @Column(nullable = false)
    private String billingKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PgProvider pgProvider;

    @Column(nullable = false)
    private boolean isDefault;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodStatus status;


    @Builder
    public SubscriptionPaymentMethod(
            Member member,
            String customerUid,
            String billingKey,
            PgProvider pgProvider,
            boolean isDefault,
            PaymentMethodStatus status
    ) {
        this.member = member;
        this.customerUid = customerUid;
        this.billingKey = billingKey;
        this.pgProvider = pgProvider;
        this.isDefault = isDefault;
        this.status = status;
    }


    public void updateBillingKey(String newBillingKey) {
        this.billingKey = newBillingKey;
        this.status = PaymentMethodStatus.ACTIVE;
    }
}
