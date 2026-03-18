package com.paymentapp.api.order;

import com.paymentapp.core.annotation.LoginUser;
import com.paymentapp.core.dto.LoginUserInfoDto;
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
            @LoginUser LoginUserInfoDto loginUser,
            @RequestBody CreateOrderRequest request
    ) {
        return orderService.createOrder(loginUser, request);
    }

    // 주문 목록 조회
    @GetMapping
    public List<OrderListResponse> getOrders(
            @LoginUser LoginUserInfoDto loginUser
    ) {
        return orderService.getOrders(loginUser);
    }

    // 주문 단건 조회
    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrder(
            @LoginUser LoginUserInfoDto loginUser,
            @PathVariable Long orderId
    ) {
        return orderService.getOrder(loginUser, orderId);
    }
}