package com.paymentapp.api.plan.entity;

import com.paymentapp.api.plan.enums.BillingCycle;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String planId; // NOOB, BROKER, BLACK_HAND

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingCycle billingCycle;

    @Column(nullable = false)
    private boolean active;

    public Plan(String planId, String name, BigDecimal amount, BillingCycle billingCycle, boolean active) {
        this.planId = planId;
        this.name = name;
        this.amount = amount;
        this.billingCycle = billingCycle;
        this.active = active;
    }
}