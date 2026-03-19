package com.paymentapp.api.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebhookRequest (
        @JsonProperty("webhook_id")
        String webhookId,
        @JsonProperty("payment_id")
        String paymentKey,
        @JsonProperty("status")
        String eventStatus
) {

}
