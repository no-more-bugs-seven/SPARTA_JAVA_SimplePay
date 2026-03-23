package com.paymentapp.api.webhook;

import com.paymentapp.api.payment.PaymentService;
import com.paymentapp.api.payment.dto.ConfirmPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentService paymentService;
    private final WebhookRepository webhookRepository;

    @Transactional
    public ConfirmPaymentResponse processWebhook(String webhookId, String paymentKey, String eventStatus) {
        // 1. 웹훅 중복 수신 체크 (Unique ID)
        if (webhookRepository.existsByWebhookId(webhookId)) {
            log.info("Duplicate Webhook ignored: {}", webhookId);
            return null;
        }

        // 2. 웹훅 수신 기록 생성 (RECEIVED)
        Webhook event = Webhook.builder()
                .webhookId(webhookId)
                .paymentKey(paymentKey)
                .eventStatus(eventStatus)
                .build();
        webhookRepository.save(event);

        try {
            // 3. 이벤트 상태별 결제 확정 처리 + 웹훅 상태 변경
            switch (eventStatus) {
                case "Transaction.Paid":
                case "Transaction.Failed":
                case "Transaction.Cancelled":
                    ConfirmPaymentResponse response = paymentService.confirmPayment(paymentKey);
                    String result = response.status();

                    if ("PAID".equals(result) || "CANCELLED".equals(result) || "REFUNDED".equals(result)) event.markAsProcessed();
                    else log.info("Payment not finished yet. paymentId={}", paymentKey);
                    return response;
                default:
                    log.info("Ignored event type: {}", eventStatus);
            }

        } catch (Exception e) {
            log.error("Webhook processing failed. webhookId={}, paymentId={}", webhookId, paymentKey, e);
            event.markAsFailed();
            throw e;
        }
        return null;
    }
}
