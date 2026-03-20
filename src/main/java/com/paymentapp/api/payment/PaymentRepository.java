package com.paymentapp.api.payment;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.order.Order;
import com.paymentapp.api.payment.entity.Payment;
import com.paymentapp.api.payment.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // PESSIMISTIC_WRITE: 쓰기 락을 걸어 다른 트랜잭션의 읽기/쓰기를 모두 차단
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.paymentKey = :paymentKey")
    Optional<Payment> findByPaymentKeyWithLock(String paymentKey);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.member = :member AND p.status = :status")
    BigDecimal sumAmountByMemberAndStatus(Member member, PaymentStatus status);

    Optional<Payment> findByOrder(Order order);
}
