package com.paymentapp.api.subscription;

import com.paymentapp.api.subscription.entity.SubscriptionPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPaymentMethodRepository extends JpaRepository<SubscriptionPaymentMethod, Long> {

}
