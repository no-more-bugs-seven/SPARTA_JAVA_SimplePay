package com.paymentapp.api.subscription;

import com.paymentapp.api.plan.entity.Subscription;
import com.paymentapp.api.plan.entity.SubscriptionStatus;
import com.paymentapp.api.subscription.entity.Subscription;
import com.paymentapp.api.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdAndUserId(Long subscriptionId, Long userId);

    Optional<Subscription> findByMemberIdAndStatus(Long memberId, SubscriptionStatus subscriptionStatus);
    Optional<Subscription> findFirstByUserIdAndStatusInAndCurrentPeriodEndAfterOrderByIdDesc(
            Long userId,
            List<SubscriptionStatus> statuses,
            LocalDateTime now
    );
}
