package com.paymentapp.api.membership;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberRepository;
import com.paymentapp.api.membership.dto.MembershipPolicyResponse;
import com.paymentapp.api.membership.dto.MyMembershipResponse;
import com.paymentapp.api.payment.PaymentRepository;
import com.paymentapp.api.payment.entity.PaymentStatus;
import com.paymentapp.core.exception.errorcode.MembershipErrorCode;
import com.paymentapp.core.exception.custom.MembershipException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipService {

    private final MembershipTierRepository membershipTierRepository;
    private final MembershipHistoryRepository membershipHistoryRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public MyMembershipResponse getMyMembership(Long userId) {
        Member member = getMember(userId);

        // 히스토리 기준 최신 등급 반환
        MembershipTier tier = membershipHistoryRepository.findByMemberOrderByChangedAtDesc(member)
                .stream()
                .findFirst()
                .map(MembershipHistory::getTier)
                .orElseGet(() -> membershipTierRepository.findAll()
                        .stream()
                        .min((a, b) -> a.getMinSpentAmount().compareTo(b.getMinSpentAmount()))
                        .orElseThrow(() -> new MembershipException(MembershipErrorCode.TIER_NOT_FOUND))
                );

        return MyMembershipResponse.from(tier);
    }

    public List<MembershipPolicyResponse> getAllPolicies() {
        return membershipTierRepository.findAll()
                .stream()
                .map(MembershipPolicyResponse::from)
                .toList();
    }

    @Transactional
    public void updateMembershipTier(Member member, BigDecimal totalSpentAmount) {
        MembershipTier newTier = membershipTierRepository
                .findTopByMinSpentAmountLessThanEqualOrderByMinSpentAmountDesc(totalSpentAmount)
                .orElseThrow(() -> new MembershipException(MembershipErrorCode.TIER_NOT_FOUND));

        membershipHistoryRepository.save(
                MembershipHistory.builder()
                        .member(member)
                        .tier(newTier)
                        .changedAt(LocalDateTime.now())
                        .build()
        );
        member.updateMembershipTier(newTier.getId());
    }

    // PAID 상태 결제 금액 합산 (결제 완료/환불 후 등급 재계산 시 호출)
    public BigDecimal calculateTotalSpentAmount(Member member) {
        return paymentRepository.sumAmountByMemberAndStatus(member, PaymentStatus.PAID);
    }

    public BigDecimal getPointRate(Member member) {

        // 히스토리 기준 최신 등급의 적립률 반환
        return membershipHistoryRepository.findByMemberOrderByChangedAtDesc(member)
                .stream()
                .findFirst()
                .map(history -> history.getTier().getPointRate())
                .orElseGet(() -> membershipTierRepository.findAll()
                        .stream()
                        .min((a, b) -> a.getMinSpentAmount().compareTo(b.getMinSpentAmount()))
                        .orElseThrow(() -> new MembershipException(MembershipErrorCode.TIER_NOT_FOUND))
                        .getPointRate()
                );
    }

    private Member getMember(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new MembershipException(MembershipErrorCode.USER_NOT_FOUND));
    }
}
