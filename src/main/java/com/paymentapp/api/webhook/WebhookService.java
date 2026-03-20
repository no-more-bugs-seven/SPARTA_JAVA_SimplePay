package com.paymentapp.api.webhook;

import com.paymentapp.api.payment.PaymentService;
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
    public void processWebhook(String webhookId, String paymentKey, String eventStatus) {
        // 1. 웹훅 중복 수신 체크 (Unique ID)
        if (webhookRepository.existsByWebhookId(webhookId)) {
            log.info("Duplicate Webhook ignored: {}", webhookId);
            return;
        }

        // 2. 웹훅 수신 기록 생성 (RECEIVED)
        Webhook event = Webhook.builder()
                .webhookId(webhookId)
                .paymentKey(paymentKey)
                .eventStatus(eventStatus)
                .build();
        webhookRepository.save(event);

        try {
            // 3. 이벤트 타입별 처리
            switch (eventStatus) {
                case "Transaction.Paid":
                    paymentService.confirmPayment(paymentKey);
                    event.markAsProcessed();
                    break;
                case "Transaction.Failed":
                    paymentService.failPayment(paymentKey);
                    event.markAsProcessed();
                    break;
                default:
                    log.info("Ignored event type: {}", eventStatus);
            }

        } catch (Exception e) {
            log.error("Webhook processing failed. webhookId={}, paymentId={}", webhookId, paymentKey, e);
            event.markAsFailed();
            throw e;
        }
    }
}
