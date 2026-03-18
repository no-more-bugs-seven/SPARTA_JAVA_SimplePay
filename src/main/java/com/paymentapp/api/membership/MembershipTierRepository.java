package com.paymentapp.api.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {
    // 총 결제 금액 기준으로 해당하는 등급 조회
    Optional<MembershipTier> findTopByMinSpentAmountLessThanEqualOrderByMinSpentAmountDesc(BigDecimal totalSpent);
}
