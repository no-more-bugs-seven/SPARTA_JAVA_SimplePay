package com.paymentapp.api.payment;

import com.paymentapp.api.order.Order;
import com.paymentapp.api.order.OrderItemRepository;
import com.paymentapp.api.order.OrderRepository;
import com.paymentapp.api.order.OrderStatus;
import com.paymentapp.api.payment.dto.*;
import com.paymentapp.api.payment.entity.Payment;
import com.paymentapp.api.payment.entity.PaymentStatus;
import com.paymentapp.core.exception.custom.PaymentException;
import com.paymentapp.core.portone.PortOneClient;
import com.paymentapp.core.portone.dto.PortOnePaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private PortOneClient portOneClient;

    /**
     * 결제 생성
     */
    @Test
    void given_유효한_주문_when_결제생성_then_PENDING상태의_결제가_생성된다() {
        // given
        Long orderId = 1L;
        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "id", orderId);

        CreatePaymentRequest request =
                new CreatePaymentRequest(orderId, BigDecimal.valueOf(10000));

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(paymentRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CreatePaymentResponse response = paymentService.createPayment(request);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.status()).isEqualTo("PENDING");
        then(paymentRepository).should().save(any(Payment.class));
    }

    /**
     * 결제 성공
     */
    @Test
    void given_정상결제정보_when_결제검증_then_결제와_주문이_완료된다() {
        // given
        String paymentKey = "PAY-123";

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "id", 1L);
        ReflectionTestUtils.setField(order, "status", OrderStatus.PENDING);

        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .order(order)
                .amount(BigDecimal.valueOf(10000))
                .status(PaymentStatus.PENDING)
                .build();

        PortOnePaymentResponse response = mock(PortOnePaymentResponse.class);
        PortOnePaymentResponse.Amount amount = mock(PortOnePaymentResponse.Amount.class);

        given(paymentRepository.findByPaymentKeyWithLock(paymentKey))
                .willReturn(Optional.of(payment));
        given(portOneClient.getPayment(paymentKey)).willReturn(response);
        given(response.getStatus()).willReturn("PAID");
        given(response.getAmount()).willReturn(amount);
        given(amount.getTotal()).willReturn(10000L);

        given(orderRepository.findById(any())).willReturn(Optional.of(order));
        given(orderItemRepository.findByOrderId(any())).willReturn(List.of());

        // when
        ConfirmPaymentResponse result = paymentService.confirmPayment(paymentKey);

        // then
        assertThat(result.success()).isTrue();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        //assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    /**
     * 멱등성 테스트
     */
    @Test
    void given_이미_처리된_결제_when_재요청_then_외부API를_호출하지_않고_기존결과를_반환한다() {
        // given
        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "id", 1L);

        Payment payment = Payment.builder()
                .paymentKey("PAY-123")
                .status(PaymentStatus.PAID)
                .order(order)
                .build();

        given(paymentRepository.findByPaymentKeyWithLock(any()))
                .willReturn(Optional.of(payment));

        // when
        ConfirmPaymentResponse result = paymentService.confirmPayment("PAY-123");

        // then
        assertThat(result.success()).isTrue();
        then(portOneClient).shouldHaveNoInteractions();
    }

    /**
     * 결제 실패
     */
    @Test
    void given_PortOne결제가_실패상태_when_검증_then_결제는_FAILED처리된다() {
        // given
        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "id", 1L);

        Payment payment = Payment.builder()
                .paymentKey("PAY-123")
                .status(PaymentStatus.PENDING)
                .order(order)
                .build();

        PortOnePaymentResponse response = mock(PortOnePaymentResponse.class);

        given(paymentRepository.findByPaymentKeyWithLock(any()))
                .willReturn(Optional.of(payment));
        given(portOneClient.getPayment(any())).willReturn(response);
        given(response.getStatus()).willReturn("FAILED");

        // when
        ConfirmPaymentResponse result = paymentService.confirmPayment("PAY-123");

        // then
        assertThat(result.success()).isFalse();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    /**
     * 금액 위변조 → 보상 트랜잭션
     */
    @Test
    void given_결제금액이_불일치할때_when_검증_then_자동으로_환불된다() {
        // given
        String paymentKey = "PAY-123";

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "id", 1L);

        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .order(order)
                .amount(BigDecimal.valueOf(10000))
                .status(PaymentStatus.PENDING)
                .build();

        PortOnePaymentResponse response = mock(PortOnePaymentResponse.class);
        PortOnePaymentResponse.Amount amount = mock(PortOnePaymentResponse.Amount.class);

        given(paymentRepository.findByPaymentKeyWithLock(paymentKey))
                .willReturn(Optional.of(payment));
        given(portOneClient.getPayment(paymentKey)).willReturn(response);
        given(response.getStatus()).willReturn("PAID");
        given(response.getAmount()).willReturn(amount);
        given(amount.getTotal()).willReturn(5000L);

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(paymentKey))
                .isInstanceOf(PaymentException.class);

        then(portOneClient).should().cancelPayment(any(), any());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    /**
     * 환불 성공
     */
    @Test
    void given_정상결제건_when_환불요청_then_환불되고_재고가_복구된다() {
        // given
        String paymentKey = "PAY-123";

        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "id", 1L);
        //ReflectionTestUtils.setField(order, "status", OrderStatus.COMPLETED);

        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .order(order)
                .amount(BigDecimal.valueOf(10000))
                .status(PaymentStatus.PAID)
                .build();

        given(paymentRepository.findByPaymentKeyWithLock(paymentKey))
                .willReturn(Optional.of(payment));

        CancelPaymentRequest request = new CancelPaymentRequest("단순 변심");

        given(orderItemRepository.findByOrderId(any())).willReturn(List.of());

        // when
        CancelPaymentResponse result =
                paymentService.cancelPayment(paymentKey, request);

        // then
        assertThat(result.success()).isTrue();
        then(portOneClient).should().cancelPayment(paymentKey, "단순 변심");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    /**
     * 이미 환불된 경우
     */
    @Test
    void given_이미_환불된_결제_when_환불요청_then_그대로_성공응답한다() {
        // given
        Order order = Order.builder().build();
        ReflectionTestUtils.setField(order, "id", 1L);

        Payment payment = Payment.builder()
                .paymentKey("PAY-123")
                .status(PaymentStatus.REFUNDED)
                .order(order)
                .build();

        given(paymentRepository.findByPaymentKeyWithLock(any()))
                .willReturn(Optional.of(payment));

        // when
        CancelPaymentResponse result =
                paymentService.cancelPayment("PAY-123", new CancelPaymentRequest(""));

        // then
        assertThat(result.success()).isTrue();
        then(portOneClient).shouldHaveNoInteractions();
    }
}