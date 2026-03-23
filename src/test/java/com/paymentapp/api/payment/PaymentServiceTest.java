package com.paymentapp.api.payment;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.membership.MembershipService;
import com.paymentapp.api.order.Order;
import com.paymentapp.api.order.OrderItem;
import com.paymentapp.api.order.OrderItemRepository;
import com.paymentapp.api.order.OrderRepository;
import com.paymentapp.api.payment.dto.*;
import com.paymentapp.api.payment.entity.Payment;
import com.paymentapp.api.payment.entity.PaymentStatus;
import com.paymentapp.api.payment.entity.Refund;
import com.paymentapp.api.point.PointService;
import com.paymentapp.api.product.Product;
import com.paymentapp.api.product.ProductRepository;
import com.paymentapp.api.order.OrderStatus;
import com.paymentapp.core.exception.custom.PaymentException;
import com.paymentapp.core.exception.custom.ProductException;
import com.paymentapp.core.portone.PortOneClient;
import com.paymentapp.core.portone.dto.PortOnePaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock private PaymentRepository paymentRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private RefundRepository refundRepository;
    @Mock private PortOneClient portOneClient;
    @Mock private PointService pointService;
    @Mock private MembershipService membershipService;

    private Member mockMember;
    private Product mockProduct;
    private Order mockOrder;
    private List<OrderItem> mockOrderItems;
    private Payment mockPayment;
    private final String paymentId = "PAY-12345";

    @BeforeEach
    void setUp() {
        mockMember = Member.builder().build();
        ReflectionTestUtils.setField(mockMember, "id", 1L);

        mockOrder = Order.builder()
                .member(mockMember)
                .build();
        ReflectionTestUtils.setField(mockOrder, "id", 1L);
        ReflectionTestUtils.setField(mockOrder, "usedPoints", BigDecimal.ZERO);
        ReflectionTestUtils.setField(mockOrder, "status", OrderStatus.PENDING);

        mockPayment = Payment.builder()
                .order(mockOrder)
                .paymentKey(paymentId)
                .amount(BigDecimal.valueOf(10000))
                .status(PaymentStatus.PENDING)
                .build();

        mockProduct = Product.builder()
                .stock(10)
                .build();
        ReflectionTestUtils.setField(mockProduct, "id", 1L);

        OrderItem item = new OrderItem(mockOrder, mockProduct, 2);
        mockOrderItems = List.of(item);
    }

    /**
     * 결제 생성 (일반 결제)
     */
    @Test
    @DisplayName("Given 유효한 주문이 있을 때, When 결제 생성을 요청하면, Then PENDING 상태의 결제가 저장되어야 한다.")
    void createPayment_Success() {
        // given
        CreatePaymentRequest request = new CreatePaymentRequest(mockOrder.getId(), BigDecimal.valueOf(10000));

        given(orderRepository.findById(mockOrder.getId())).willReturn(Optional.of(mockOrder));
        given(paymentRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        CreatePaymentResponse response = paymentService.createPayment(request);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.status()).isEqualTo("PENDING");
        then(paymentRepository).should().save(any(Payment.class));
    }

    /**
     * 결제 생성 (포인트 결제)
     */
    @Test
    @DisplayName("Given 포인트 사용이 포함된 주문일 때, When 결제 생성을 요청하면, Then 포인트가 먼저 차감되어야 한다.")
    void createPayment_WithPoints_Success() {
        // given
        ReflectionTestUtils.setField(mockOrder, "usedPoints", BigDecimal.valueOf(5000));
        CreatePaymentRequest request = new CreatePaymentRequest(mockOrder.getId(), BigDecimal.valueOf(15000));

        given(orderRepository.findById(mockOrder.getId())).willReturn(Optional.of(mockOrder));
        given(paymentRepository.save(any(Payment.class))).willReturn(mockPayment);

        // when
        paymentService.createPayment(request);

        // then
        then(pointService).should().spendPoints(eq(mockMember), eq(mockOrder), eq(BigDecimal.valueOf(5000)));
    }

    /**
     * 결제 검증 및 확정 (성공)
     */
    @Test
    @DisplayName("Given 정상 결제 데이터가 주어질 때, When 결제를 확정하면, Then 재고가 차감되고 주문/결제가 완료 상태로 변경된다.")
    void confirmPayment_Success() {
        // given
        PortOnePaymentResponse portOneResponse = mock(PortOnePaymentResponse.class);
        PortOnePaymentResponse.Amount portOneAmount = mock(PortOnePaymentResponse.Amount.class);

        given(portOneResponse.getStatus()).willReturn("PAID");
        given(portOneResponse.getAmount()).willReturn(portOneAmount);
        given(portOneAmount.getTotal()).willReturn(10000L);

        given(paymentRepository.findByPaymentKeyWithLock(paymentId)).willReturn(Optional.of(mockPayment));
        given(portOneClient.getPayment(paymentId)).willReturn(portOneResponse);
        given(orderItemRepository.findByOrderId(mockOrder.getId())).willReturn(mockOrderItems);
        given(productRepository.findAllByIdsWithLock(anyList())).willReturn(List.of(mockProduct));
        given(membershipService.getPointRate(mockMember)).willReturn(BigDecimal.valueOf(0.05)); // 5% 적립률

        // when
        ConfirmPaymentResponse response = paymentService.confirmPayment(paymentId);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(mockPayment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        then(pointService).should().earnPoints(eq(mockMember), eq(mockOrder), any(BigDecimal.class));
        then(membershipService).should().updateMembershipTier(eq(mockMember), any());
    }

    /**
     * 결제 검증 및 확정 (금액 불일치)
     */
    @Test
    @DisplayName("Given 포트원 결제 금액과 DB 금액이 다를 때, When 결제를 확정하면, Then 예외를 던지고 보상 트랜잭션이 실행된다.")
    void confirmPayment_AmountMismatch_Compensation() {
        // given
        PortOnePaymentResponse portOneResponse = mock(PortOnePaymentResponse.class);
        PortOnePaymentResponse.Amount portOneAmount = mock(PortOnePaymentResponse.Amount.class);

        // DB는 10000인데 포트원은 30000 반환
        given(portOneResponse.getStatus()).willReturn("PAID");
        given(portOneResponse.getAmount()).willReturn(portOneAmount);
        given(portOneAmount.getTotal()).willReturn(30000L);

        given(paymentRepository.findByPaymentKeyWithLock(paymentId)).willReturn(Optional.of(mockPayment));
        given(portOneClient.getPayment(paymentId)).willReturn(portOneResponse);

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(paymentId))
                .isInstanceOf(PaymentException.class);

        then(portOneClient).should().cancelPayment(eq(paymentId), contains("결제 금액 불일치"));
        assertThat(mockPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    /**
     * 결제 검증 및 확정 (재고 부족)
     */
    @Test
    @DisplayName("Given 결제 확정 중 재고가 부족할 때, When 예외가 발생하면, Then 보상 트랜잭션이 실행되어 포인트 복구와 자동 취소가 처리된다.")
    void confirmPayment_OutOfStock_Compensation() {
        // given
        PortOnePaymentResponse portOneResponse = mock(PortOnePaymentResponse.class);
        PortOnePaymentResponse.Amount portOneAmount = mock(PortOnePaymentResponse.Amount.class);

        given(portOneResponse.getStatus()).willReturn("PAID");
        given(portOneResponse.getAmount()).willReturn(portOneAmount);
        given(portOneAmount.getTotal()).willReturn(10000L);

        given(paymentRepository.findByPaymentKeyWithLock(paymentId)).willReturn(Optional.of(mockPayment));
        given(portOneClient.getPayment(paymentId)).willReturn(portOneResponse);
        given(orderItemRepository.findByOrderId(mockOrder.getId())).willReturn(mockOrderItems);
        given(productRepository.findAllByIdsWithLock(anyList())).willReturn(List.of(mockProduct));

        // 재고 감소 로직 강제 에러 발생 (차감 시 0 이하)
        ReflectionTestUtils.setField(mockProduct, "stock", 1); // 재고 1개인데 주문 2개 상황 가정

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(paymentId))
                .isInstanceOf(ProductException.class);

        then(portOneClient).should().cancelPayment(eq(paymentId), contains("재고 부족"));
        then(pointService).should().recoverPoints(mockMember, mockOrder);
    }

    /**
     * 결제 취소 및 환불 (성공)
     */
    @Test
    @DisplayName("Given 완료된 결제가 있을 때, When 취소를 요청하면, Then 외부 취소 API를 호출하고 재고와 포인트가 복구된다.")
    void cancelPayment_Success() {
        // given
        // 취소를 위해 주문과 결제를 완료(PAID, COMPLETED) 상태로 강제 조정
        ReflectionTestUtils.setField(mockPayment, "status", PaymentStatus.PAID);
        ReflectionTestUtils.setField(mockOrder, "status", OrderStatus.PAID);

        CancelPaymentRequest request = new CancelPaymentRequest("단순 변심");
        given(paymentRepository.findByPaymentKeyWithLock(paymentId)).willReturn(Optional.of(mockPayment));
        given(orderItemRepository.findByOrderId(mockOrder.getId())).willReturn(List.of());

        // when
        CancelPaymentResponse response = paymentService.cancelPayment(paymentId, request);

        // then
        assertThat(response.success()).isTrue();
        assertThat(mockPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);

        then(portOneClient).should().cancelPayment(paymentId, "단순 변심");
        then(pointService).should().recoverPoints(mockMember, mockOrder);
        then(pointService).should().cancelEarnedPoints(mockMember, mockOrder);
        then(refundRepository).should(atLeastOnce()).save(any(Refund.class));
    }

    /**
     * 결제 취소 및 환불 (이미 환불된 결제)
     */
    @Test
    @DisplayName("Given 이미 환불된 결제일 때, When 다시 취소를 요청하면, Then 멱등성 검증에 의해 로직이 무시되고 성공 응답을 반환한다.")
    void cancelPayment_Idempotency() {
        // given
        // 취소를 위해 주문과 결제를 완료(PAID, COMPLETED) 상태로 강제 조정
        ReflectionTestUtils.setField(mockPayment, "status", PaymentStatus.PAID);
        ReflectionTestUtils.setField(mockOrder, "status", OrderStatus.PAID);

        ReflectionTestUtils.setField(mockPayment, "status", PaymentStatus.REFUNDED);
        CancelPaymentRequest request = new CancelPaymentRequest("중복 요청");

        given(paymentRepository.findByPaymentKeyWithLock(paymentId)).willReturn(Optional.of(mockPayment));

        // when
        CancelPaymentResponse response = paymentService.cancelPayment(paymentId, request);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.status()).isEqualTo("REFUNDED");

        // 멱등성 검증: 추가 취소 API가 중복 호출되지 않았음을 보증
        then(portOneClient).should(never()).cancelPayment(anyString(), anyString());
        then(refundRepository).should(never()).save(any(Refund.class));
    }

}