package com.paymentapp.api.order.exception;

import com.paymentapp.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "OR001", "존재하지 않는 주문입니다."),
    INVALID_REFUND_STATE(HttpStatus.CONFLICT, "OR002", "환불 가능한 상태가 아닙니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
