package com.paymentapp.api.membership;

import com.paymentapp.api.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipHistoryRepository extends JpaRepository<MembershipHistory, Long> {
    List<MembershipHistory> findByMemberOrderByChangedAtDesc(Member member);
}
