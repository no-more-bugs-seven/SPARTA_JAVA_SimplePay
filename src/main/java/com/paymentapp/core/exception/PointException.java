package com.paymentapp.core.exception;

import com.paymentapp.core.exception.BusinessException;
import com.paymentapp.core.exception.ErrorCode;

public class PointException extends BusinessException {

    public PointException(ErrorCode errorCode) {
        super(errorCode);
    }
}
