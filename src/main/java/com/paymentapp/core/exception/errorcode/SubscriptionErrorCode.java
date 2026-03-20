package com.paymentapp.core.exception.errorcode;

import com.paymentapp.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubscriptionErrorCode implements ErrorCode {
    // 구독
    ACTIVE_SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Sc0001", "현재 이용 중인 구독이 없습니다."),
    CANCELABLE_SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Sc0002", "해지할 구독 정보가 없습니다."),
    SUBSCRIPTION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "Sc0003", "이미 종료된 구독은 해지할 수 없습니다."),
    ALREADY_SUBSCRIBED(HttpStatus.BAD_REQUEST, "Sc0003", "이미 활성화된 구독이 존재합니다. (1인 1구독)"),

    // 포트원
    INVALID_BILLING_KEY(HttpStatus.BAD_REQUEST, "Sc1001", "유효하지 않은 빌링키입니다. 결제 수단 등록에 실패했습니다."),
    FAILURE_PAYMENT(HttpStatus.BAD_REQUEST, "Sc1002", "결제에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
