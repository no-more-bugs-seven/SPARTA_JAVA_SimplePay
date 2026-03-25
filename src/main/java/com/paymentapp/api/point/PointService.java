package com.paymentapp.api.point;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberRepository;
import com.paymentapp.api.order.Order;
import com.paymentapp.api.point.dto.PointTransactionResponse;
import com.paymentapp.core.constant.PointConstants;
import com.paymentapp.core.constant.PointTransactionType;
import com.paymentapp.api.point.exception.PointErrorCode;
import com.paymentapp.api.point.exception.PointException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointTransactionRepository pointTransactionRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void earnPoints(Member member, Order order, BigDecimal points) {
        pointTransactionRepository.save(
                PointTransaction.builder()
                        .member(member)
                        .order(order)
                        .points(points)
                        .transactionType(PointTransactionType.EARNED)
                        .expiredAt(LocalDateTime.now().plusDays(PointConstants.POINT_EXPIRY_DAYS))
                        .build()
        );
        member.addPointBalance(points);
    }


    @Transactional
    public void spendPoints(Member member, Order order, BigDecimal points) {

        pointTransactionRepository.save(
                PointTransaction.builder()
                        .member(member)
                        .order(order)
                        .points(points)
                        .transactionType(PointTransactionType.SPENT)
                        .expiredAt(null)
                        .build()
        );
        member.subtractPointBalance(points);
    }

    @Transactional
    public void recoverPoints(Member member, Order order) {
        List<PointTransaction> spentList = pointTransactionRepository
                .findByMemberAndOrderAndTransactionType(member, order, PointTransactionType.SPENT);
        BigDecimal totalRecovered = BigDecimal.ZERO;
        for (PointTransaction spent : spentList) {
            pointTransactionRepository.save(
                    PointTransaction.builder()
                            .member(member)
                            .order(order)
                            .points(spent.getPoints())
                            .transactionType(PointTransactionType.RECOVERED)
                            .expiredAt(null)
                            .build()
            );
            totalRecovered = totalRecovered.add(spent.getPoints());
        }
        // 스냅샷 갱신: 복구된 총 포인트만큼 pointBalance 증가
        if (totalRecovered.compareTo(BigDecimal.ZERO) > 0) {
            member.addPointBalance(totalRecovered);
        }
    }

    @Transactional
    public void cancelEarnedPoints(Member member, Order order) {
        List<PointTransaction> earnedList = pointTransactionRepository
                .findByMemberAndOrderAndTransactionType(member, order, PointTransactionType.EARNED);
        BigDecimal totalCanceled = BigDecimal.ZERO;
        for (PointTransaction earned : earnedList) {
            pointTransactionRepository.save(
                    PointTransaction.builder()
                            .member(member)
                            .order(order)
                            .points(earned.getPoints())
                            .transactionType(PointTransactionType.CANCELED)
                            .expiredAt(null)
                            .build()
            );
            totalCanceled = totalCanceled.add(earned.getPoints());
        }
        // 스냅샷 갱신: 취소된 총 포인트만큼 pointBalance 차감
        if (totalCanceled.compareTo(BigDecimal.ZERO) > 0) {
            member.subtractPointBalance(totalCanceled);
        }
    }

    @Transactional
    public void expirePoints() {
        List<PointTransaction> expiredList = pointTransactionRepository
                .findUnexpiredEarnedTransactions(LocalDateTime.now());

        for (PointTransaction earned : expiredList) {
            pointTransactionRepository.save(
                    PointTransaction.builder()
                            .member(earned.getMember())
                            .order(null)
                            .points(earned.getPoints())
                            .transactionType(PointTransactionType.EXPIRED)
                            .expiredAt(null)
                            .build()
            );
            // 스냅샷 갱신: 소멸 포인트만큼 개별 member의 pointBalance 차감
            earned.getMember().subtractPointBalance(earned.getPoints());
        }
    }

    public List<PointTransactionResponse> getMyTransactions(Long userId) {
        Member member = getMember(userId);
        return pointTransactionRepository.findByMemberOrderByCreatedAtDesc(member)
                .stream()
                .map(PointTransactionResponse::from)
                .toList();
    }

    private Member getMember(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new PointException(PointErrorCode.USER_NOT_FOUND));
    }
}