package com.paymentapp.api.payment;

import com.github.f4b6a3.tsid.TsidCreator;
import com.paymentapp.api.order.Order;
import com.paymentapp.api.order.OrderItem;
import com.paymentapp.api.order.OrderItemRepository;
import com.paymentapp.api.order.OrderRepository;
import com.paymentapp.api.payment.dto.ConfirmPaymentResponse;
import com.paymentapp.api.payment.dto.CreatePaymentRequest;
import com.paymentapp.api.payment.dto.CreatePaymentResponse;
import com.paymentapp.api.product.Product;
import com.paymentapp.api.product.ProductRepository;
import com.paymentapp.core.exception.CommonErrorCode;
import com.paymentapp.core.exception.MemberException;
import com.paymentapp.core.portone.PortOneClient;
import com.paymentapp.core.portone.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PortOneClient portOneClient;

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        // 주문 조회
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new MemberException(CommonErrorCode.NOT_FOUND));
        // 결제 시도 생성
        String paymentKey = "PAY-" + TsidCreator.getTsid();

        Payment payment = Payment.builder()
                .order(order)
                .paymentKey(paymentKey)
                .amount(request.totalAmount())
                .status(PaymentStatus.PENDING)
                .build();
        // 결제 저장
        Payment savedPayment = paymentRepository.save(payment);

        return CreatePaymentResponse.of(
                true,
                savedPayment.getPaymentKey(),
                savedPayment.getStatus().toString()
        );
    }

    @Transactional
    public ConfirmPaymentResponse confirmPayment(String paymentId) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findByPaymentKey(paymentId)
                .orElseThrow(() -> new MemberException(CommonErrorCode.NOT_FOUND));

        // 2. 멱등성 체크 (이미 처리된 경우)
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return ConfirmPaymentResponse.of(
                    false,
                    payment.getOrder().getId().toString(),
                    payment.getStatus().toString()
            );
        }

        // 3. PortOne 결제 조회
        PortOnePaymentResponse response = portOneClient.getPayment(paymentId);

        // 4. 결제 실패
        if (!"PAID".equals(response.getStatus())) {
            payment.fail();
            return ConfirmPaymentResponse.of(
                    false,
                    payment.getOrder().getId().toString(),
                    payment.getStatus().toString()
            );
        }

        // 5. 금액 검증
        BigDecimal orderAmount = payment.getAmount();
        BigDecimal paidAmount = BigDecimal.valueOf(response.getAmount().getTotal());
        if (orderAmount.compareTo(paidAmount) != 0) {
            throw new MemberException(CommonErrorCode.NOT_FOUND);
        }

        // 6. 주문 조회
        Order order = orderRepository.findById(payment.getOrder().getId())
                .orElseThrow(() -> new MemberException(CommonErrorCode.NOT_FOUND));

        // 7. 재고 차감
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new MemberException(CommonErrorCode.NOT_FOUND));

            product.decreaseStock(item.getQuantity());
        }

        // 8. 결제/주문 상태 변경
        payment.complete();
        //order.complete();

        // 9. 응답 반환
        return ConfirmPaymentResponse.of(
                true,
                payment.getOrder().getId().toString(),
                payment.getStatus().toString()
        );
    }
}
