package com.paymentapp.core.exception.errorcode;

import com.paymentapp.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubscriptionErrorCode implements ErrorCode {
    // 구독
    ACTIVE_SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Sc001", "현재 이용 중인 구독이 없습니다."),
    CANCELABLE_SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Sc002", "해지할 구독 정보가 없습니다."),
    SUBSCRIPTION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "Sc003", "이미 종료된 구독은 해지할 수 없습니다."),
    ALREADY_SUBSCRIBED(HttpStatus.BAD_REQUEST, "Sc004", "이미 활성화된 구독이 존재합니다. (1인 1구독)"),
    INVALID_SUBSCRIPTION_STATUS_MESSAGE(HttpStatus.BAD_REQUEST, "Sc004", "ACTIVE 상태의 구독만 플랜 변경이 가능합니다."),
    SAME_PLAN_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Sc005", "현재 이용 중인 플랜과 동일한 플랜으로는 변경할 수 없습니다."),
    INVALID_SUBSCRIPTION_ID(HttpStatus.BAD_REQUEST, "Sc006", "subscriptionId 형식이 올바르지 않습니다."),



    // 포트원
    INVALID_BILLING_KEY(HttpStatus.BAD_REQUEST, "Sc101", "유효하지 않은 빌링키입니다. 결제 수단 등록에 실패했습니다."),
    FAILURE_PAYMENT(HttpStatus.BAD_REQUEST, "Sc102", "결제에 실패했습니다."),
    ALREADY_BILLED_PERIOD(HttpStatus.BAD_REQUEST, "Sc102", "이미 결제한 내역이 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
