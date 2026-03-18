package com.paymentapp.core.exception.errorcode;

import com.paymentapp.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR001", "포인트 잔액이 부족합니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "PR002", "포인트 잔액이 부족합니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
