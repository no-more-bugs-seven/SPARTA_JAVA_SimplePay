package com.paymentapp.api.subscription;

import com.paymentapp.api.subscription.entity.SubscriptionBilling;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SubscriptionBillingRepository extends JpaRepository<SubscriptionBilling, Long> {

}
