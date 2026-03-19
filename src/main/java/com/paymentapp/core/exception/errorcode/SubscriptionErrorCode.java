package com.paymentapp.core.exception.errorcode;

import com.paymentapp.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubscriptionErrorCode implements ErrorCode {
    ACTIVE_SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Sc002", "현재 이용 중인 구독이 없습니다."),
    CANCELABLE_SUBSCRIPTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "Sc001", "해지할 구독 정보가 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
