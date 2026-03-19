package com.paymentapp.core.exception.custom;

import com.paymentapp.core.exception.ErrorCode;

public class PointException extends BusinessException {

    public PointException(ErrorCode errorCode) {
        super(errorCode);
    }
}
