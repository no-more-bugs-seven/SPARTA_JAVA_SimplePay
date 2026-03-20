package com.paymentapp.api.point;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.order.Order;
import com.paymentapp.core.constant.PointTransactionType;
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
@Table(name = "point_transactions")
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private BigDecimal points;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private PointTransactionType transactionType;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Builder
    private PointTransaction(Member member, Order order, BigDecimal points, PointTransactionType transactionType, LocalDateTime expiredAt) {
        this.member = member;
        this.order = order;
        this.points = points;
        this.transactionType = transactionType;
        this.expiredAt = expiredAt;
    }
}
