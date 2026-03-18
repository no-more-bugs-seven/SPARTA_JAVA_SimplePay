package com.paymentapp.api.webhook;

import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    private String webhookId;
    private String paymentKey;
    private String eventStatus;

    private WebhookStatus status;

    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
}
