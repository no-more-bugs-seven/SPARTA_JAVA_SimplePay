package com.paymentapp.api.point.exception;

import com.paymentapp.core.exception.ErrorCode;
import com.paymentapp.core.exception.custom.BusinessException;

public class PointException extends BusinessException {

    public PointException(ErrorCode errorCode) {
        super(errorCode);
    }
}
