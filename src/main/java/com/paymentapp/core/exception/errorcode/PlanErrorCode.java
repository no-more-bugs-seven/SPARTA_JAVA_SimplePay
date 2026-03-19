package com.paymentapp.core.exception.errorcode;

import com.paymentapp.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlanErrorCode implements ErrorCode {
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "Sc002", "존재하지 않는 플랜입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
