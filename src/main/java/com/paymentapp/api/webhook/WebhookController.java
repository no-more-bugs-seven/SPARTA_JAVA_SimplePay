package com.paymentapp.api.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/portone")
    public ResponseEntity<Void> handlePortOneWebhook(
            @RequestHeader("webhook-id") String webhookId,
            /*@RequestHeader("webhook-signature") String signature,*/
            @RequestBody Map<String, Object> request
    ) {
        Map<String, Object> data = (Map<String, Object>) request.get("data");
        String paymentKey = (String) data.get("paymentId");
        String eventStatus = (String) request.get("type");

        webhookService.processWebhook(webhookId, paymentKey, eventStatus);
        return ResponseEntity.ok().build();
    }
}
