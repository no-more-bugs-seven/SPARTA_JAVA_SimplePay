package com.paymentapp.api.webhook;

import com.paymentapp.api.payment.dto.ConfirmPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookService webhookService;
    private final SseEmitterRepository emitterRepository;

    @PostMapping("/portone")
    public ResponseEntity<Void> handlePortOneWebhook(
            @RequestHeader("webhook-id") String webhookId,
            /*@RequestHeader("webhook-signature") String signature,*/
            @RequestBody Map<String, Object> request
    ) {
        Map<String, Object> data = (Map<String, Object>) request.get("data");
        String paymentKey = (String) data.get("paymentId");
        String eventStatus = (String) request.get("type");

        ConfirmPaymentResponse response = webhookService.processWebhook(webhookId, paymentKey, eventStatus);

        SseEmitter emitter = emitterRepository.get(paymentKey);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("payment")
                        .data(response));
            } catch (Exception e) {
                emitterRepository.delete(paymentKey);
            }
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/sse")
    public SseEmitter sse(@RequestParam String paymentId) {
        SseEmitter emitter = new SseEmitter(0L);

        emitterRepository.save(paymentId, emitter);

        emitter.onCompletion(() -> emitterRepository.delete(paymentId));
        emitter.onTimeout(() -> emitterRepository.delete(paymentId));

        return emitter;
    }
}
