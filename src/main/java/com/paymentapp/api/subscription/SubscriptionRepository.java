package com.paymentapp.api.subscription;

import com.paymentapp.api.subscription.entity.Subscription;
import com.paymentapp.api.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdAndMemberId(Long subscriptionId, Long memberId);

    boolean existsByMemberIdAndStatus(Long memberId, SubscriptionStatus status);

    List<Subscription> findAllByStatusAndNextPaymentAtBefore(SubscriptionStatus subscriptionStatus, LocalDateTime now);

}