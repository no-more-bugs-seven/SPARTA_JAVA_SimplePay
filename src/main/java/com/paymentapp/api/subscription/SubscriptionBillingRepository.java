package com.paymentapp.api.subscription;

import com.paymentapp.api.subscription.entity.SubscriptionBilling;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;


public interface SubscriptionBillingRepository extends JpaRepository<SubscriptionBilling, Long> {


    List<SubscriptionBilling> findBySubscriptionIdOrderByAttemptedAtDesc(Long subscriptionId);
}
