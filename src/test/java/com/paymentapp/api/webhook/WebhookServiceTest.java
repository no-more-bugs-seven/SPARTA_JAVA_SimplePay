package com.paymentapp.api.webhook;

import com.paymentapp.api.payment.PaymentService;
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

    /**
     * 중복 웹훅 처리
     */
    @Test
    void given_이미_처리된_웹훅_when_수신_then_아무작업도_하지않는다() {
        // given
        String webhookId = "wh_123";

        given(webhookRepository.existsByWebhookId(webhookId))
                .willReturn(true);

        // when
        webhookService.processWebhook(webhookId, "PAY-1", "Transaction.Paid");

        // then
        then(webhookRepository).should(never()).save(any());
        then(paymentService).shouldHaveNoInteractions();
    }

    /**
     * 정상 결제 웹훅 처리
     */
    @Test
    void given_정상_결제_웹훅_when_처리_then_결제확정이_호출되고_상태가_processed로_변경된다() {
        // given
        String webhookId = "wh_123";
        String paymentKey = "PAY-123";

        given(webhookRepository.existsByWebhookId(webhookId))
                .willReturn(false);

        Webhook webhook = Webhook.builder()
                .webhookId(webhookId)
                .paymentKey(paymentKey)
                .eventStatus("Transaction.Paid")
                .build();

        given(webhookRepository.save(any())).willReturn(webhook);

        // when
        webhookService.processWebhook(webhookId, paymentKey, "Transaction.Paid");

        // then
        then(paymentService).should().confirmPayment(paymentKey);
        assertThat(webhook.getStatus()).isEqualTo(WebhookStatus.PROCESSED);
    }

    /**
     * 결제 실패 -> 웹훅 실패 처리
     */
    @Test
    void given_결제처리중_예외발생_when_웹훅처리_then_상태는_failed로_변경되고_예외가_전파된다() {
        // given
        String webhookId = "wh_123";
        String paymentKey = "PAY-123";

        given(webhookRepository.existsByWebhookId(webhookId))
                .willReturn(false);

        Webhook webhook = Webhook.builder()
                .webhookId(webhookId)
                .paymentKey(paymentKey)
                .eventStatus("Transaction.Paid")
                .build();

        given(webhookRepository.save(any())).willReturn(webhook);

        willThrow(new RuntimeException("결제 실패"))
                .given(paymentService)
                .confirmPayment(paymentKey);

        // when & then
        assertThatThrownBy(() ->
                webhookService.processWebhook(webhookId, paymentKey, "Transaction.Paid")
        ).isInstanceOf(RuntimeException.class);

        assertThat(webhook.getStatus()).isEqualTo(WebhookStatus.FAILED);
    }

    /**
     * 결제 이벤트가 아닌 경우
     */
    @Test
    void given_결제이벤트가_아닌_웹훅_when_처리_then_결제서비스는_호출되지않는다() {
        // given
        String webhookId = "wh_123";

        given(webhookRepository.existsByWebhookId(webhookId))
                .willReturn(false);

        Webhook webhook = Webhook.builder()
                .webhookId(webhookId)
                .paymentKey("PAY-123")
                .eventStatus("Transaction.Ready")
                .build();

        given(webhookRepository.save(any())).willReturn(webhook);

        // when
        webhookService.processWebhook(webhookId, "PAY-123", "Transaction.Ready");

        // then
        then(paymentService).shouldHaveNoInteractions();
    }
}