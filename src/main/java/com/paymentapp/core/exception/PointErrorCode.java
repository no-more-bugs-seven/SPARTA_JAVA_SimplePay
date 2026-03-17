package com.paymentapp.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "P001", "포인트 잔액이 부족합니다."),
    POINT_TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "포인트 거래 내역을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "존재하지 않는 사용자입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
