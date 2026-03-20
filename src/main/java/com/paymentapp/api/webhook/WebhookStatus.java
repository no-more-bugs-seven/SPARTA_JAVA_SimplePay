package com.paymentapp.api.webhook;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebhookStatus {
    RECEIVED("수신완료"),
    PROCESSED("처리완료"),
    FAILED("처리실패");

    private final String description;
}
