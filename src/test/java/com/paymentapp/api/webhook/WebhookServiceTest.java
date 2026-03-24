package com.paymentapp.api.webhook;

import com.paymentapp.api.payment.PaymentService;
import com.paymentapp.api.payment.dto.ConfirmPaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @InjectMocks
    private WebhookService webhookService;

    @Mock private PaymentService paymentService;
    @Mock private WebhookRepository webhookRepository;

    private final String webhookId = "WH-123456";
    private final String paymentKey = "PAY-12345";

    /**
     * 웹훅 수신 (성공)
     */
    @Test
    @DisplayName("Given 새로운 웹훅이 수신되었을 때, When 결제 완료 이벤트를 처리하면, Then 결제 확정 로직이 호출되고 웹훅이 완료 상태가 된다.")
    void givenNewPaidWebhook_WhenProcess_ThenPaymentConfirmed() {
        // given
        String eventStatus = "Transaction.Paid";
        ConfirmPaymentResponse mockResponse = new ConfirmPaymentResponse(true, "1", "COMPLETED");

        given(webhookRepository.existsByWebhookId(webhookId)).willReturn(false);
        given(paymentService.confirmPayment(paymentKey)).willReturn(mockResponse);

        // when
        webhookService.processWebhook(webhookId, paymentKey, eventStatus);

        // then
        then(webhookRepository).should().save(any(Webhook.class)); // 수신 기록 저장 확인
        then(paymentService).should().confirmPayment(paymentKey); // 실제 결제 서비스 호출 확인
    }

    /**
     * 웹훅 수신 (이미 처리된 웹훅 ID)
     */
    @Test
    @DisplayName("Given 이미 처리된 웹훅 ID가 들어왔을 때, When 프로세스를 실행하면, Then 추가 로직 없이 종료된다. (중복 방지)")
    void givenDuplicateWebhook_WhenProcess_ThenIgnore() {
        // given
        given(webhookRepository.existsByWebhookId(webhookId)).willReturn(true);

        // when
        webhookService.processWebhook(webhookId, paymentKey, "Transaction.Paid");

        // then
        then(webhookRepository).should(never()).save(any(Webhook.class)); // 저장되지 않아야 함
        then(paymentService).should(never()).confirmPayment(anyString()); // 결제 서비스 호출되지 않아야 함
    }

    /**
     * 웹훅 수신 (결제 실패시 웹훅 수신)
     */
    @Test
    @DisplayName("Given 결제 실패 이벤트를 수신했을 때, When 프로세스를 실행하면, Then 결제 서비스에 실패 상태를 확인하고 웹훅을 완료 처리한다.")
    void givenFailedWebhook_WhenProcess_ThenMarkAsProcessed() {
        // given
        String eventStatus = "Transaction.Failed";
        ConfirmPaymentResponse mockResponse = new ConfirmPaymentResponse(false, "1", "CANCELLED");

        given(webhookRepository.existsByWebhookId(webhookId)).willReturn(false);
        given(paymentService.confirmPayment(paymentKey)).willReturn(mockResponse);

        // when
        webhookService.processWebhook(webhookId, paymentKey, eventStatus);

        // then
        then(paymentService).should().confirmPayment(paymentKey);
    }

    /**
     * 웹훅 수신 (수신 실패)
     */
    @Test
    @DisplayName("Given 처리 중 예외가 발생했을 때, When 프로세스를 실행하면, Then 웹훅 상태를 실패로 변경하고 예외를 다시 던진다.")
    void givenException_WhenProcess_ThenMarkAsFailedAndThrow() {
        // given
        given(webhookRepository.existsByWebhookId(webhookId)).willReturn(false);
        given(paymentService.confirmPayment(paymentKey)).willThrow(new RuntimeException("Webhook processing failed."));

        // when & then
        assertThatThrownBy(() ->
                webhookService.processWebhook(webhookId, paymentKey, "Transaction.Paid")
        ).isInstanceOf(RuntimeException.class);

        // then: 실패 기록은 남아야 함
        then(webhookRepository).should().save(any(Webhook.class));
    }
}