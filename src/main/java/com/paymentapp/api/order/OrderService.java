package com.paymentapp.api.order;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberRepository;
import com.paymentapp.api.payment.PaymentRepository;
import com.paymentapp.api.point.PointTransactionRepository;
import com.paymentapp.api.point.dto.PointTransactionResponse;
import com.paymentapp.api.product.Product;
import com.paymentapp.api.product.ProductRepository;
import com.paymentapp.api.order.dto.CreateOrderRequest;
import com.paymentapp.api.order.dto.OrderCreateResponse;
import com.paymentapp.api.order.dto.OrderDetailResponse;
import com.paymentapp.api.order.dto.OrderListResponse;
import com.paymentapp.core.dto.LoginUserInfoDto;
import com.paymentapp.core.exception.custom.PointException;
import com.paymentapp.core.exception.errorcode.PointErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final PointTransactionRepository pointTransactionRepository;

    // 주문 생성
    public OrderCreateResponse createOrder(LoginUserInfoDto loginUser, CreateOrderRequest request) {

        Member member = memberRepository.findById(loginUser.id())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("주문 상품은 최소 1개 이상이어야 합니다.");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order(member, BigDecimal.ZERO);

        for (CreateOrderRequest.OrderItemRequest item : request.getItems()) {

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
            }

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            //product.decreaseStock(item.getQuantity());

            OrderItem orderItem = new OrderItem(order, product, item.getQuantity());
            order.addOrderItem(orderItem);

            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        // 총 금액 반영
        order.updateTotalAmount(totalAmount);

        // 포인트 사용 처리
        BigDecimal usedPoints = request.getUsedPoints() != null ? request.getUsedPoints() : BigDecimal.ZERO;
        if (usedPoints.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pointBalance = member.getPointBalance() != null ? member.getPointBalance() : BigDecimal.ZERO;
            if (usedPoints.compareTo(pointBalance) > 0) throw new PointException(PointErrorCode.INSUFFICIENT_POINTS);
            if (usedPoints.compareTo(totalAmount) > 0) throw new PointException(PointErrorCode.POINTS_EXCEED_ORDER_AMOUNT);
            order.applyUsedPoints(usedPoints);
            order.updateTotalAmount(totalAmount.subtract(usedPoints));
        }

        Order savedOrder = orderRepository.save(order);

        return OrderCreateResponse.builder()
                .orderId(savedOrder.getId().toString())
                .totalAmount(totalAmount)
                .usedPoints(savedOrder.getUsedPoints())
                .finalAmount(savedOrder.getTotalAmount())
                .orderNumber(savedOrder.getOrderNumber())
                .build();
    }

    // 주문 목록 조회
    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrders(LoginUserInfoDto loginUser) {

        Member member = memberRepository.findById(loginUser.id())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        List<OrderListResponse> list = orderRepository.findByMemberOrderByCreatedAtDesc(member)
                .stream()
                .map(order -> OrderListResponse.builder()
                        .orderId(order.getId().toString())
                        .orderNumber(order.getOrderNumber())
                        .totalAmount(order.getTotalAmount())
                        .usedPoints(order.getUsedPoints())
                        .finalAmount(order.getTotalAmount().subtract(order.getUsedPoints()))
                        .earnedPoints(order.getEarnedPoints())
                        .currency("")
                        .status(order.getStatus().name())
                        .createdAt(order.getCreatedAt().toString())
                        .build())
                .toList();
        return list;
    }

    // 주문 단건 조회
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(LoginUserInfoDto loginUser, Long orderId) {

        Member member = memberRepository.findById(loginUser.id())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Order order = orderRepository.findByIdAndMember(orderId, member)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 결제 정보 조회
        OrderDetailResponse.PaymentDto paymentDto = paymentRepository.findByOrder(order)
                .map(p -> OrderDetailResponse.PaymentDto.builder()
                        .paymentKey(p.getPaymentKey())
                        .amount(p.getAmount())
                        .status(p.getStatus().name())
                        .paidAt(p.getPaidAt())
                        .build())
                .orElse(OrderDetailResponse.PaymentDto.builder().build());

        // 포인트 사용/적립 내역 조회
        List<PointTransactionResponse> pointTransactions = pointTransactionRepository
                .findByOrderOrderByCreatedAtDesc(order)
                .stream()
                .map(PointTransactionResponse::from)
                .toList();

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount().add(order.getUsedPoints())) // 원가 합계
                .usedPoints(order.getUsedPoints())
                .finalAmount(order.getTotalAmount()) // 실결제 금액
                .createdAt(order.getCreatedAt())
                .items(order.getOrderItems().stream()
                        .map(item -> OrderDetailResponse.OrderItemDto.builder()
                                .productName(item.getProductName())
                                .productPrice(item.getProductPrice())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
    }
}