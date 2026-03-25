package com.paymentapp.api.payment.exception;

import com.paymentapp.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PA001", "존재하지 않는 결제입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PA002", "금액이 일치하지 않습니다."),
    INVALID_REFUND_STATE(HttpStatus.CONFLICT, "PA003", "환불 가능한 상태가 아닙니다."),
    INVALID_WEBHOOK_SIGNATURE(HttpStatus.CONFLICT, "PA004", "웹훅 서명 검증이 실패했습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.CONFLICT, "PA005", "잘못된 결제 상태값입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
