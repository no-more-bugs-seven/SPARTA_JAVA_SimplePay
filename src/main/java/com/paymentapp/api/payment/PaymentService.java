package com.paymentapp.api.payment;

import com.github.f4b6a3.tsid.TsidCreator;
import com.paymentapp.api.order.Order;
import com.paymentapp.api.order.OrderRepository;
import com.paymentapp.api.payment.dto.CreatePaymentRequest;
import com.paymentapp.api.payment.dto.CreatePaymentResponse;
import com.paymentapp.core.exception.CommonErrorCode;
import com.paymentapp.core.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

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
                .status(PaymentStatus.PENDING.toString())
                .build();
        // 결제 저장
        Payment savedPayment = paymentRepository.save(payment);

        return CreatePaymentResponse.of(savedPayment.getPaymentKey());
    }
}
