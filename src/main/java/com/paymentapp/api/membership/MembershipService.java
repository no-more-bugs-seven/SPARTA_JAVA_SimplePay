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

        // 스냅샷 직접 사용 — 히스토리 조회 불필요
        MembershipTier tier = member.getMembershipTier();
        if (tier == null) {
            throw new MembershipException(MembershipErrorCode.TIER_NOT_FOUND);
        }

        return MyMembershipResponse.from(tier);
    }

    public List<MembershipPolicyResponse> getAllPolicies() {
        return membershipTierRepository.findAll()
                .stream()
                .map(MembershipPolicyResponse::from)
                .toList();
    }

    // 회원 가입 시 Normal 등급 조회 (MemberService에서 Member 생성 시 Builder에 전달)
    public MembershipTier getNormalTier() {
        return membershipTierRepository
                .findTopByMinSpentAmountLessThanEqualOrderByMinSpentAmountDesc(BigDecimal.ZERO)
                .orElseThrow(() -> new MembershipException(MembershipErrorCode.TIER_NOT_FOUND));
    }

    // 회원 가입 시 Normal 등급 히스토리 기록 (등급 세팅은 Member 생성 시 Builder에서 처리)
    @Transactional
    public void initMembership(Member member) {
        membershipHistoryRepository.save(
                MembershipHistory.builder()
                        .member(member)
                        .tier(member.getMembershipTier())
                        .build()
        );
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
                        .build()
        );
        member.updateMembershipTier(newTier);
    }

    // PAID 상태 결제 금액 합산 (결제 완료/환불 후 등급 재계산 시 호출)
    public BigDecimal calculateTotalSpentAmount(Member member) {
        return paymentRepository.sumAmountByMemberAndStatus(member, PaymentStatus.PAID);
    }

    public BigDecimal getPointRate(Member member) {
        // 스냅샷 직접 사용 — 히스토리 조회 불필요
        MembershipTier tier = member.getMembershipTier();
        if (tier == null) {
            throw new MembershipException(MembershipErrorCode.TIER_NOT_FOUND);
        }
        return tier.getPointRate();
    }

    private Member getMember(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new MembershipException(MembershipErrorCode.USER_NOT_FOUND));
    }
}
