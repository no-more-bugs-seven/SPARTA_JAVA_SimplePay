package com.paymentapp.api.webhook;

import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "webhooks")
public class Webhook extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "webhook_id", nullable = false, unique = true)
    private String webhookId;
    @Column(name = "payment_id", nullable = false)
    private String paymentKey;
    @Column(name = "event_status")
    private String eventStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookStatus status;

    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;

    @Builder
    public Webhook(String webhookId, String paymentKey, String eventStatus) {
        this.webhookId = webhookId;
        this.paymentKey = paymentKey;
        this.eventStatus = eventStatus;
        this.status = WebhookStatus.RECEIVED;
        this.receivedAt = LocalDateTime.now();
    }

    public void markAsProcessed() {
        this.status = WebhookStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = WebhookStatus.FAILED;
    }
}
