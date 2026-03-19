package com.paymentapp.core.exception.custom;

import com.paymentapp.core.exception.ErrorCode;
import lombok.Getter;

/**
 * 모든 비즈니스 로직 예외의 최상위 클래스
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}