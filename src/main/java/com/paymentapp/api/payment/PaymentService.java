package com.paymentapp.api.payment;

import com.github.f4b6a3.tsid.TsidCreator;
import com.paymentapp.api.order.Order;
import com.paymentapp.api.order.OrderItem;
import com.paymentapp.api.order.OrderItemRepository;
import com.paymentapp.api.order.OrderRepository;
import com.paymentapp.api.payment.dto.*;
import com.paymentapp.api.payment.entity.Payment;
import com.paymentapp.api.payment.entity.PaymentStatus;
import com.paymentapp.api.payment.entity.Refund;
import com.paymentapp.api.payment.entity.RefundStatus;
import com.paymentapp.api.product.Product;
import com.paymentapp.api.product.ProductRepository;
import com.paymentapp.core.constant.OrderStatus;
import com.paymentapp.core.exception.CommonErrorCode;
import com.paymentapp.core.exception.MemberException;
import com.paymentapp.core.portone.PortOneClient;
import com.paymentapp.core.portone.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RefundRepository refundRepository;
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
        // 1. 결제 조회 (Lock)
        Payment payment = paymentRepository.findByPaymentKeyWithLock(paymentId)
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

        // 4. 결제 상태 확인
        if (!"PAID".equals(response.getStatus())) {
            payment.fail();
            return ConfirmPaymentResponse.of(
                    false,
                    payment.getOrder().getId().toString(),
                    "FAILED"
            );
        }

        // 5. 금액 검증
        BigDecimal orderAmount = payment.getAmount();
        BigDecimal paidAmount = BigDecimal.valueOf(response.getAmount().getTotal());
        if (orderAmount.compareTo(paidAmount) != 0) {
            // [보안] 금액 위변조 감지 시 자동 취소 로직
            /*portOneClient.cancelPayment(paymentId, "결제 금액 불일치(위변조 의심)");
            payment.fail();*/
            throw new MemberException(CommonErrorCode.NOT_FOUND);
        }

        // 6. 주문 조회
        Order order = orderRepository.findById(payment.getOrder().getId())
                .orElseThrow(() -> new MemberException(CommonErrorCode.NOT_FOUND));

        try {
            // 7. 재고 차감
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

            for (OrderItem item : items) {
                // Product에도 Lock걸어 동시 재고 차감 방지
                Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                        .orElseThrow(() -> new MemberException(CommonErrorCode.NOT_FOUND));

                product.decreaseStock(item.getQuantity());
            }

            // 8. 결제/주문 상태 변경
            payment.complete();
            order.complete();

        } catch (IllegalArgumentException e) {
            // 재고가 없으면 포트원에 즉시 결제 취소 요청
            portOneClient.cancelPayment(paymentId, "재고 부족으로 인한 자동 결제 취소");
            payment.fail();
            throw e;
        }

        // 9. 응답 반환
        return ConfirmPaymentResponse.of(
                true,
                payment.getOrder().getId().toString(),
                "PAID"
        );
    }

    @Transactional
    public CancelPaymentResponse cancelPayment(String paymentId, CancelPaymentRequest request) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findByPaymentKeyWithLock(paymentId)
                .orElseThrow(() -> new MemberException(CommonErrorCode.NOT_FOUND));

        // 2. 주문 조회
        Order order = orderRepository.findById(payment.getOrder().getId())
                .orElseThrow(() -> new MemberException(CommonErrorCode.NOT_FOUND));

        // 3. 멱등성 체크 (이미 환불된 경우)
        Optional<Refund> existingRefund = refundRepository.findByPaymentId(payment.getId());
        if (existingRefund.isPresent()) {
            return CancelPaymentResponse.of(
                    false,
                    payment.getPaymentKey(),
                    payment.getStatus().toString()
            );
        }

        // 4. 상태 검증
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("환불 가능한 상태가 아닙니다.");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("환불 가능한 상태가 아닙니다.");
        }

        // 4. 환불 이력 생성 (REQUESTED)
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(payment.getAmount())
                .reason(request.reason())
                .status(RefundStatus.REQUESTED)
                .build();

        refundRepository.save(refund);

        try {
            // 5. PortOne 환불 API 호출
            portOneClient.cancelPayment(paymentId, request.reason());

            // 6. 상태 변경
            payment.updateStatus(PaymentStatus.REFUNDED);
            payment.getOrder().updateStatus(OrderStatus.REFUNDED);

            refund = Refund.builder()
                    .payment(refund.getPayment())
                    .amount(refund.getAmount())
                    .reason(refund.getReason())
                    .status(RefundStatus.COMPLETED)
                    .refundedAt(LocalDateTime.now())
                    .build();

            refundRepository.save(refund);

        } catch (Exception e) {
            refund = Refund.builder()
                    .payment(refund.getPayment())
                    .amount(refund.getAmount())
                    .reason(refund.getReason())
                    .status(RefundStatus.FAILED)
                    .build();

            refundRepository.save(refund);

            throw e;
        }

        return CancelPaymentResponse.of(
                true,
                order.getId().toString(),
                order.getStatus().toString()
        );
    }
}
