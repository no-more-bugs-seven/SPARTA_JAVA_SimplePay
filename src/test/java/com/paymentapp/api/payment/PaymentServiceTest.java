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
import com.paymentapp.api.payment.entity.RefundStatus;
import com.paymentapp.api.point.PointService;
import com.paymentapp.api.product.Product;
import com.paymentapp.api.product.ProductRepository;
import com.paymentapp.api.order.OrderStatus;
import com.paymentapp.core.exception.custom.PaymentException;
import com.paymentapp.core.exception.custom.ProductException;
import com.paymentapp.core.exception.errorcode.ProductErrorCode;
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
        ReflectionTestUtils.setField(mockOrder, "totalAmount", BigDecimal.valueOf(15000));
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
        given(paymentRepository.findFirstByOrderAndStatusInOrderByCreatedAtDesc(any(), anyList()))
                .willReturn(Optional.empty());
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
        // 주문 15000원 중 10000원만 결제 요청 -> 5000원 포인트 사용 상황
        CreatePaymentRequest request = new CreatePaymentRequest(mockOrder.getId(), BigDecimal.valueOf(10000));

        given(orderRepository.findById(mockOrder.getId())).willReturn(Optional.of(mockOrder));
        given(paymentRepository.findFirstByOrderAndStatusInOrderByCreatedAtDesc(any(), anyList()))
                .willReturn(Optional.empty());
        given(paymentRepository.save(any(Payment.class))).willReturn(mockPayment);

        // when
        paymentService.createPayment(request);

        // then
        // usedPoints = 15000 - 10000 = 5000
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
        given(membershipService.getPointRate(mockMember)).willReturn(BigDecimal.valueOf(0.05));

        // when
        ConfirmPaymentResponse response = paymentService.confirmPayment(paymentId);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(mockPayment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        then(pointService).should().earnPoints(eq(mockMember), eq(mockOrder), any(BigDecimal.class));
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
        assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    /**
     * 결제 검증 및 확정 (재고 부족)
     */
    @Test
    @DisplayName("Given 결제 확정 중 재고가 부족할 때, When 예외가 발생하면, Then 보상 트랜잭션이 실행되어 포인트 복구와 자동 취소가 처리된다.")
    void confirmPayment_OutOfStock_Compensation() {
        // given
        // 1. 포트원 결제 결과 준비 (성공 상태)
        PortOnePaymentResponse portOneResponse = mock(PortOnePaymentResponse.class);
        PortOnePaymentResponse.Amount portOneAmount = mock(PortOnePaymentResponse.Amount.class);

        given(portOneResponse.getStatus()).willReturn("PAID");
        given(portOneResponse.getAmount()).willReturn(portOneAmount);
        given(portOneAmount.getTotal()).willReturn(10000L); // 결제 금액 10,000원

        // 2. DB 상태 준비 (결제 대기 중)
        given(paymentRepository.findByPaymentKeyWithLock(paymentId)).willReturn(Optional.of(mockPayment));
        given(portOneClient.getPayment(paymentId)).willReturn(portOneResponse);

        // 3. 재고 부족 상황 연출
        // OrderItemRepository는 아이템 리스트를 반환하지만,
        // ProductRepository가 반환한 상품 리스트 중 하나가 재고 차감 시 예외를 던지도록 설정
        given(orderItemRepository.findByOrderId(mockOrder.getId())).willReturn(mockOrderItems);

        // MockProduct의 실제 동작을 스파이하거나,
        // 서비스 로직 내 changeStock 내부 루프에서 예외가 터지도록 MockProduct를 구성합니다.
        Product outOfStockProduct = spy(Product.builder()
                .stock(1) // 재고는 1개
                .build());
        ReflectionTestUtils.setField(outOfStockProduct, "id", 1L);

        // mockOrderItems의 아이템이 2개 구매 요청이므로, decreaseStock 호출 시 예외 발생 시뮬레이션
        doThrow(new ProductException(ProductErrorCode.INSUFFICIENT_STOCK))
                .when(outOfStockProduct).decreaseStock(anyInt());

        given(productRepository.findAllByIdsWithLock(anyList())).willReturn(List.of(outOfStockProduct));

        // When & Then
        assertThatThrownBy(() -> paymentService.confirmPayment(paymentId))
                .isInstanceOf(ProductException.class);

        // Then
        // 보상 트랜잭션(handleCompensation) 핵심 로직 검증
        // - 외부 결제 취소 API가 호출되었는가?
        then(portOneClient).should().cancelPayment(eq(paymentId), contains("재고 부족"));
        // - 결제 생성 시 차감되었던 포인트가 복구되었는가?
        then(pointService).should().recoverPoints(eq(mockMember), eq(mockOrder));
        // - 결제와 주문 상태가 REFUNDED로 최종 변경되었는가?
        assertThat(mockPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        // - 환불 이력이 COMPLETED 상태로 저장되었는가?
        then(refundRepository).should(atLeastOnce()).save(argThat(refund ->
                refund.getStatus() == RefundStatus.COMPLETED &&
                        refund.getPayment().equals(mockPayment)
        ));
    }

    /**
     * 결제 취소 및 환불 (성공)
     */
    @Test
    @DisplayName("Given 완료된 결제가 있을 때, When 취소를 요청하면, Then 외부 취소 API를 호출하고 재고와 포인트가 복구된다.")
    void cancelPayment_Success() {
        // given
        ReflectionTestUtils.setField(mockPayment, "status", PaymentStatus.PAID);
        ReflectionTestUtils.setField(mockOrder, "status", OrderStatus.PAID);

        CancelPaymentRequest request = new CancelPaymentRequest("단순 변심");
        given(paymentRepository.findByPaymentKeyWithLock(paymentId)).willReturn(Optional.of(mockPayment));
        given(orderItemRepository.findByOrderId(mockOrder.getId())).willReturn(mockOrderItems);
        given(productRepository.findAllByIdsWithLock(anyList())).willReturn(List.of(mockProduct));

        // when
        CancelPaymentResponse response = paymentService.cancelPayment(paymentId, request);

        // then
        assertThat(response.success()).isTrue();
        assertThat(mockPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);

        then(portOneClient).should().cancelPayment(paymentId, "단순 변심");
        then(pointService).should().recoverPoints(mockMember, mockOrder);
        then(refundRepository).should(atLeastOnce()).save(any(Refund.class));
    }

    /**
     * 결제 취소 및 환불 (이미 환불된 결제)
     */
    @Test
    @DisplayName("Given 이미 환불된 결제일 때, When 다시 취소를 요청하면, Then 멱등성 검증에 의해 로직이 무시되고 성공 응답을 반환한다.")
    void cancelPayment_Idempotency() {
        // given
        CreatePaymentRequest request = new CreatePaymentRequest(mockOrder.getId(), BigDecimal.valueOf(10000));
        given(orderRepository.findById(mockOrder.getId())).willReturn(Optional.of(mockOrder));

        // PENDING 상태의 결제가 이미 존재함 시뮬레이션
        given(paymentRepository.findFirstByOrderAndStatusInOrderByCreatedAtDesc(any(), anyList()))
                .willReturn(Optional.of(mockPayment));

        // when
        CreatePaymentResponse response = paymentService.createPayment(request);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.paymentId()).isEqualTo(paymentId);
        then(paymentRepository).should(never()).save(any()); // 새로 저장하지 않음
    }

}