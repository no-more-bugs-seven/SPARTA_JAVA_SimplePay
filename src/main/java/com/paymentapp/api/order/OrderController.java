package com.paymentapp.api.order;

import com.paymentapp.api.member.Member;

import com.paymentapp.api.order.dto.CreateOrderRequest;
import com.paymentapp.api.order.dto.OrderCreateResponse;
import com.paymentapp.api.order.dto.OrderDetailResponse;
import com.paymentapp.api.order.dto.OrderListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public OrderCreateResponse createOrder(
            @RequestAttribute Member member,
            @RequestBody CreateOrderRequest request
    ) {
        return orderService.createOrder(member, request);
    }

    // 주문 목록 조회
    @GetMapping
    public List<OrderListResponse> getOrders(@RequestAttribute Member member) {
        return orderService.getOrders(member);
    }

    // 주문 단건 조회
    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrder(
            @RequestAttribute Member member,
            @PathVariable Long orderId
    ) {
        return orderService.getOrder(member, orderId);
    }
}