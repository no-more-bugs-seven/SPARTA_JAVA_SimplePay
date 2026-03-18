package com.paymentapp.api.order;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.payment.entity.PaymentStatus;
import com.paymentapp.core.constant.OrderStatus;
import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ERD의 user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    // 주문번호
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    // 주문 총액
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // 주문 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // 사용 포인트
    @Column(name = "used_points", nullable = false, precision = 12, scale = 2)
    private BigDecimal usedPoints;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> orderItems = new ArrayList<>();

    public Order(Member member, BigDecimal totalAmount) {
        this.member = member;
        this.orderNumber = UUID.randomUUID().toString();
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.usedPoints = BigDecimal.ZERO;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
    }

    public void markPaid() {
        this.status = OrderStatus.COMPLETED;
    }

    public void markCancelled() {
        this.status = OrderStatus.CANCELLED;
    }

    public void markRefunded() {
        this.status = OrderStatus.REFUNDED;
    }

    public void applyUsedPoints(BigDecimal usedPoints) {
        this.usedPoints = usedPoints;
    }

    public void updateTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    // 결제 성공시 update
    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }
}