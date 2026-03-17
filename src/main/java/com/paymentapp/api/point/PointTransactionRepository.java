package com.paymentapp.api.point;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.order.Order;
import com.paymentapp.core.constant.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByMemberOrderByCreatedAtDesc(Member member);
    List<PointTransaction> findByMemberAndOrderAndTransactionType(Member member, Order order, PointTransactionType type);
    List<PointTransaction> findByTransactionTypeAndExpiryAtBefore(PointTransactionType type, LocalDateTime now);
}