package com.paymentapp.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MembershipErrorCode implements ErrorCode {
    TIER_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "멤버십 등급을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "S002", "존재하지 않는 사용자입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
