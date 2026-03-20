package com.paymentapp.api.point;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.order.Order;
import com.paymentapp.core.constant.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByMemberOrderByCreatedAtDesc(Member member);
    List<PointTransaction> findByMemberAndOrderAndTransactionType(Member member, Order order, PointTransactionType type);
    /**
     * 만료 대상 EARNED 거래 조회 — 아직 EXPIRED 처리되지 않은 것만 반환
     *
     * 문제: findByTransactionTypeAndExpiryAtBefore() 를 그냥 쓰면
     *       EXPIRED 레코드를 생성해도 원본 EARNED 레코드의 타입은 그대로 EARNED 이므로
     *       매일 자정마다 동일한 포인트가 반복 소멸 처리됨.
     *
     * 해결: 같은 (member, order) 조합의 EXPIRED 레코드가 이미 존재하는 EARNED 는 제외
     *       - order 가 있는 경우: member + order 로 매칭
     *       - order 가 없는 경우 (ADMIN_ADJUST 등): member + order IS NULL 로 매칭
     */
    @Query("""
            SELECT e FROM PointTransaction e
            WHERE e.transactionType = 'EARNED'
              AND e.expiredAt < :now
              AND NOT EXISTS (
                  SELECT ex FROM PointTransaction ex
                  WHERE ex.transactionType = 'EXPIRED'
                    AND ex.member = e.member
                    AND (
                        (e.order IS NULL AND ex.order IS NULL)
                        OR ex.order = e.order
                    )
              )
            """)
    List<PointTransaction> findUnexpiredEarnedTransactions(LocalDateTime now);
    List<PointTransaction> findByOrderOrderByCreatedAtDesc(Order order);
}