package com.paymentapp.api.membership;

import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "membership_tiers")
public class MembershipTier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(name = "min_spent_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minSpentAmount;

    @Column(name = "point_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal pointRate;
}