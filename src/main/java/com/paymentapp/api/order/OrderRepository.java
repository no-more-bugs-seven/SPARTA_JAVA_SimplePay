package com.paymentapp.api.order;

import com.paymentapp.api.member.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"orderItems"})
    List<Order> findByMemberOrderByCreatedAtDesc(Member member);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    Optional<Order> findByIdAndMember(Long id, Member member);
}