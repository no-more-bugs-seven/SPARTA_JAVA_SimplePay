package com.paymentapp.api.webhook;

import com.paymentapp.api.payment.PaymentService;
import com.paymentapp.core.exception.custom.PaymentException;
import com.paymentapp.core.exception.errorcode.PaymentErrorCode;
import com.paymentapp.front.properties.PortOneProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentService paymentService;
    private final WebhookRepository webhookRepository;
    private final PortOneProperties portOneProperties;

    @Transactional
    public void processWebhook(String webhookId, String signature, WebhookRequest request) {
        // 1. 서명 검증 (보안)
        if (!verifySignature(signature, request)) {
            log.error("Invalid Webhook Signature: {}", webhookId);
            throw new PaymentException(PaymentErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }

        // 2. 웹훅 중복 수신 체크 (Unique ID)
        if (webhookRepository.existsByWebhookId(webhookId)) {
            log.info("Duplicate Webhook ignored: {}", webhookId);
            return;
        }

        // 3. 웹훅 수신 기록 생성 (RECEIVED)
        Webhook event = Webhook.builder()
                .webhookId(webhookId)
                .paymentKey(request.paymentKey())
                .eventStatus(request.eventStatus())
                .build();
        webhookRepository.save(event);

        try {
            // 4. 기존 결제 확정 로직 호출 (재사용)
            paymentService.confirmPayment(request.paymentKey());

            // 5. 성공 시 상태 변경
            event.markAsProcessed();
        } catch (Exception e) {
            // 6. 실패 시 상태 변경 및 예외 전파
            event.markAsFailed();
            throw e;
        }
    }

    /**
     * HMAC-SHA256을 이용한 서명 검증
     */
    private boolean verifySignature(String signature, WebhookRequest request) {
        try {
            String secret = portOneProperties.getWebhook().getSecret();

            // 포트원 시그니처는 "v1=서명값" 형태이므로 추출이 필요함
            String receivedSig = signature.replace("v1=", "");

            // 검증 대상 데이터 (포트원 가이드에 따라 바디 내용을 문자열로 조합)
            // 실제 구현 시에는 ObjectMapper를 이용해 바디 원문을 그대로 사용하거나 규격에 맞춰 조합함
            String data = request.paymentKey() + "|" + request.eventStatus();

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String calculatedSig = HexFormat.of().formatHex(hash);

            return calculatedSig.equals(receivedSig);
        } catch (Exception e) {
            return false;
        }
    }
}
