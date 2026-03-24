package com.paymentapp.core.exception.errorcode;

import com.paymentapp.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlanErrorCode implements ErrorCode {
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "존재하지 않는 플랜입니다."),
    PLAN_INACTIVE(HttpStatus.BAD_REQUEST, "P002", "비활성화된 플랜은 구독할 수 없습니다."),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "P003", "요청 금액이 플랜 금액과 일치하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
