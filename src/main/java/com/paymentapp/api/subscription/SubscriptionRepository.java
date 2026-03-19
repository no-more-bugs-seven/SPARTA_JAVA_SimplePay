package com.paymentapp.api.subscription;

import com.paymentapp.api.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdAndMemberId(Long subscriptionId, Long userId);

//    Optional<Subscription> findFirstByUserIdAndStatusInAndCurrentPeriodEndAfterOrderByIdDesc(
//            Long userId,
//            List<SubscriptionStatus> statuses,
//            LocalDateTime now
//    );
}