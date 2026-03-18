package com.paymentapp.api.webhook;

import com.paymentapp.api.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentService paymentService;
    private final WebhookRepository webhookRepository;

    /*@Transactional
    public void processWebhook(String webhookId, WebhookRequest request) {
        // 1. 웹훅 중복 수신 체크 (webhook_id UNIQUE 제약조건 활용)
        if (webhookRepository.existsByWebhookId(webhookId)) {
            return; // 이미 처리 중이거나 완료된 웹훅은 무시
        }

        // 2. 웹훅 이벤트 수신 기록 (RECEIVED)
        Webhook event = Webhook.builder()
                .webhookId(webhookId)
                .paymentKey(request.paymentId())
                .eventStatus(request.status())
                .build();
        webhookRepository.save(event);

        try {
            // 3. 기존 결제 확정 로직 호출 (공통 로직 재사용)
            // confirmPayment 내부의 비관적 락이 클라이언트 요청과 웹훅 간의 경합을 막아줌
            paymentService.confirmPayment(request.paymentId());

            // 4. 처리 완료 기록 (PROCESSED)
            event.markAsProcessed();

        } catch (Exception e) {
            // 5. 실패 기록 (FAILED)
            event.markAsFailed();
            throw e;
        }
    }*/
}
