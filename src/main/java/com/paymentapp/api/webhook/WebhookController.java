package com.paymentapp.api.webhook;

import com.paymentapp.api.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/portone")
    public ResponseEntity<Void> handlePortOneWebhook(
            @RequestHeader("webhook-id") String webhookId,
            @RequestBody WebhookRequest request
    ) {
        //webhookService.processWebhook(webhookId, request);
        return ResponseEntity.ok().build();
    }
}
