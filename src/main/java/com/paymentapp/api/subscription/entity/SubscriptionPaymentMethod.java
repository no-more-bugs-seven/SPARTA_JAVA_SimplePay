package com.paymentapp.api.subscription.entity;

import com.paymentapp.api.member.Member;
import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Column(nullable = false, unique = true)
    private String customerUid;

    @Column(nullable = false)
    private String billingKey;

    @Column(nullable = false)
    private String pgProvider;

    @Column(nullable = false)
    private boolean isDefault;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodStatus status;



}
