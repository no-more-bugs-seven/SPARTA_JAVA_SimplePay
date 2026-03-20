package com.paymentapp.api.point;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberRepository;
import com.paymentapp.api.order.Order;
import com.paymentapp.api.point.dto.PointTransactionResponse;
import com.paymentapp.core.constant.PointConstants;
import com.paymentapp.core.constant.PointTransactionType;
import com.paymentapp.core.exception.errorcode.PointErrorCode;
import com.paymentapp.core.exception.custom.PointException;
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
                        .expiryAt(LocalDateTime.now().plusDays(PointConstants.POINT_EXPIRY_DAYS))
                        .build()
        );
    }


    @Transactional
    public void spendPoints(Member member, Order order, BigDecimal points) {

        pointTransactionRepository.save(
                PointTransaction.builder()
                        .member(member)
                        .order(order)
                        .points(points)
                        .transactionType(PointTransactionType.SPENT)
                        .expiryAt(null)
                        .build()
        );
    }

    @Transactional
    public void recoverPoints(Member member, Order order) {
        List<PointTransaction> spentList = pointTransactionRepository
                .findByMemberAndOrderAndTransactionType(member, order, PointTransactionType.SPENT);

        for (PointTransaction spent : spentList) {
            pointTransactionRepository.save(
                    PointTransaction.builder()
                            .member(member)
                            .order(order)
                            .points(spent.getPoints())
                            .transactionType(PointTransactionType.RECOVERED)
                            .expiryAt(null)
                            .build()
            );
        }
    }

    @Transactional
    public void cancelEarnedPoints(Member member, Order order) {
        List<PointTransaction> earnedList = pointTransactionRepository
                .findByMemberAndOrderAndTransactionType(member, order, PointTransactionType.EARNED);

        for (PointTransaction earned : earnedList) {
            pointTransactionRepository.save(
                    PointTransaction.builder()
                            .member(member)
                            .order(order)
                            .points(earned.getPoints())
                            .transactionType(PointTransactionType.CANCELED)
                            .expiryAt(null)
                            .build()
            );
        }
    }

    @Transactional
    public void expirePoints() {
        List<PointTransaction> expiredList = pointTransactionRepository
                .findByTransactionTypeAndExpiryAtBefore(
                        PointTransactionType.EARNED, LocalDateTime.now());

        for (PointTransaction earned : expiredList) {
            pointTransactionRepository.save(
                    PointTransaction.builder()
                            .member(earned.getMember())
                            .order(null)
                            .points(earned.getPoints())
                            .transactionType(PointTransactionType.EXPIRED)
                            .expiryAt(null)
                            .build()
            );
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