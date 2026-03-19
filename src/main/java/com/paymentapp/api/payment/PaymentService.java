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
import com.paymentapp.core.exception.custom.MemberException;
import com.paymentapp.core.exception.custom.OrderException;
import com.paymentapp.core.exception.custom.PaymentException;
import com.paymentapp.core.exception.custom.ProductException;
import com.paymentapp.core.exception.errorcode.CommonErrorCode;
import com.paymentapp.core.exception.errorcode.OrderErrorCode;
import com.paymentapp.core.exception.errorcode.PaymentErrorCode;
import com.paymentapp.core.exception.errorcode.ProductErrorCode;
import com.paymentapp.core.portone.PortOneClient;
import com.paymentapp.core.portone.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RefundRepository refundRepository;
    private final PortOneClient portOneClient;

    /**
     * 결제 시도 생성
     * @param request
     * @return
     */
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        // 1. 주문 조회
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 2. 결제 시도 생성
        String paymentKey = "PAY-" + TsidCreator.getTsid();

        Payment payment = Payment.builder()
                .order(order)
                .paymentKey(paymentKey)
                .amount(request.totalAmount())
                .status(PaymentStatus.PENDING)
                .build();

        // 3. 결제 저장
        Payment savedPayment = paymentRepository.save(payment);

        // 4. 응답 반환
        return CreatePaymentResponse.of(
                true,
                savedPayment.getPaymentKey(),
                savedPayment.getStatus().toString()
        );
    }

    /**
     * 결제 검증 조회 및 확정
     * @param paymentId
     * @return
     */
    @Transactional
    public ConfirmPaymentResponse confirmPayment(String paymentId) {
        // 1. 결제 조회 (Lock)
        Payment payment = paymentRepository.findByPaymentKeyWithLock(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 2. 멱등성 체크 (이미 성공한 결제는 성공으로 응답)
        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.FAILED) {
            return ConfirmPaymentResponse.of(
                    true,
                    payment.getOrder().getId().toString(),
                    payment.getStatus().toString()
            );
        }

        // 3. PortOne 결제 조회
        PortOnePaymentResponse response = portOneClient.getPayment(paymentId);

        // 4. 결제 상태 확인
        if (!"PAID".equals(response.getStatus())) {
            payment.updateStatus(PaymentStatus.FAILED);
            return ConfirmPaymentResponse.of(
                    false,
                    payment.getOrder().getId().toString(),
                    "FAILED"
            );
        }

        // 5. 금액 검증
        BigDecimal paidAmount = BigDecimal.valueOf(response.getAmount().getTotal());
        if (payment.getAmount().compareTo(paidAmount) != 0) {
            // 보상 트랜잭션 : 금액 위변조 감지 시 자동 취소 로직
            handleCompensation(payment, payment.getOrder(), "결제 금액 불일치");
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 6. 주문 조회
        Order order = orderRepository.findById(payment.getOrder().getId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 동시에 같은 상품 재고 차감시 재고가 부족한 경우 예외처리
        try {
            // 7. 재고 차감
            changeStock(payment.getOrder(), "decrease");

            // 8. 결제/주문 상태 변경
            payment.complete();
            order.updateStatus(OrderStatus.COMPLETED);

        } catch (ProductException e) {
            // 보상 트랜잭션 : 재고 부족시 자동 결제 취소
            handleCompensation(payment, order, "재고 부족으로 인한 자동 결제 취소");
            throw e;
        } catch (Exception e) {
            // 보상 트랜잭션 : 그외 서버 내부 오류시 자동 취소
            handleCompensation(payment, order, "시스템 오류로 인한 자동 취소");
            throw e;
        }

        // 9. 응답 반환
        return ConfirmPaymentResponse.of(
                true,
                payment.getOrder().getId().toString(),
                "PAID"
        );
    }

    /**
     * 결제 취소 및 환불
     * @param paymentId
     * @param request
     * @return
     */
    @Transactional
    public CancelPaymentResponse cancelPayment(String paymentId, CancelPaymentRequest request) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findByPaymentKeyWithLock(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 2. 멱등성 체크: 이미 환불된 상태라면 성공으로 간주하고 현재 상태 반환
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return CancelPaymentResponse.of(
                    true,
                    payment.getOrder().getId().toString(),
                    "REFUNDED");
        }

        // 3. 상태 검증 (주문 완료 및 결제 완료 상태인지)
        validateRefundableState(payment);

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

            // 6. 재고 원상 복구
            changeStock(payment.getOrder(), "restore");

            // 7. 상태 변경
            payment.updateStatus(PaymentStatus.REFUNDED);
            payment.getOrder().updateStatus(OrderStatus.REFUNDED);

            // 8. 환불 성공 이력 추가 (COMPLETED)
            refund = Refund.builder()
                    .payment(refund.getPayment())
                    .amount(refund.getAmount())
                    .reason(refund.getReason())
                    .status(RefundStatus.COMPLETED)
                    .refundedAt(LocalDateTime.now())
                    .build();
            refundRepository.save(refund);

        } catch (Exception e) {
            // 9. 환불 실패 이력 추가 (FAILED)
            refund = Refund.builder()
                    .payment(refund.getPayment())
                    .amount(refund.getAmount())
                    .reason(refund.getReason())
                    .status(RefundStatus.FAILED)
                    .build();
            refundRepository.save(refund);

            throw e;
        }

        // 10. 응답 반환
        return CancelPaymentResponse.of(
                true,
                payment.getOrder().getId().toString(),
                "REFUNDED"
        );
    }

    /**
     * 보상 트랜잭션 공통 로직 (환불 처리 및 상태 변경)
     */
    private void handleCompensation(Payment payment, Order order, String reason) {
        try {
            // 1. 결제 취소 API 호출
            portOneClient.cancelPayment(payment.getPaymentKey(), reason);

        } catch (Exception e) {
            log.error("결제 취소 API 호출 실패 - paymentKey: {}", payment.getPaymentKey(), e);
        }

        // 2. 상태 변경
        payment.updateStatus(PaymentStatus.REFUNDED);
        order.updateStatus(OrderStatus.REFUNDED);

        // 3. 환불 이력 생성
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(payment.getAmount())
                .reason(reason)
                .status(RefundStatus.COMPLETED)
                .refundedAt(LocalDateTime.now())
                .build();
        refundRepository.save(refund);
    }

    /**
     * 결제 환불시 상태 검증
     */
    private void validateRefundableState(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new PaymentException(PaymentErrorCode.INVALID_REFUND_STATE);
        }
        if (payment.getOrder().getStatus() != OrderStatus.COMPLETED) {
            throw new OrderException(OrderErrorCode.INVALID_REFUND_STATE);
        }
    }

    /**
     * 재고 원상복구 로직 (비관적 일괄 락 사용)
     */
    private void changeStock(Order order, String type) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        List<Long> productIds = items.stream()
                .map(item -> item.getProduct().getId())
                .toList();

        // 상품들에 대해 비관적 락 획득 (줄 세우기)
        List<Product> products = productRepository.findAllByIdsWithLock(productIds);

        // 10. 재고 차감
        for (OrderItem item : items) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

            if ("restore".equals(type)) product.increaseStock(item.getQuantity());
            else product.decreaseStock(item.getQuantity());
        }
    }
}
