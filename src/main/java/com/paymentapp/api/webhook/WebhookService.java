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
    public void processWebhook(String webhookId, String signature, String paymentKey, String eventStatus) {
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

        if (eventStatus.equals("Transaction.Paid")) {
            try {
                // 3. 기존 결제 확정 로직 호출
                paymentService.confirmPayment(paymentKey);

                // 4. 성공 시 상태 변경
                event.markAsProcessed();
            } catch (Exception e) {
                // 5. 실패 시 상태 변경 및 예외 전파
                event.markAsFailed();
                throw e;
            }
        }
    }
}
