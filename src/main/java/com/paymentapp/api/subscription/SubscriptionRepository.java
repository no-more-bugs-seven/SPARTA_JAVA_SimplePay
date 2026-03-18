package com.paymentapp.api.subscription;

import com.paymentapp.api.subscription.entity.Subscription;
import com.paymentapp.api.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {


    Optional<Subscription> findByMemberIdAndStatus(Long memberId, SubscriptionStatus subscriptionStatus);
}
