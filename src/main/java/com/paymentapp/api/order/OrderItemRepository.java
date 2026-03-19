package com.paymentapp.api.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            select oi from OrderItem oi
            join fetch oi.product
            where oi.order.id = :orderId
            """)
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
}
