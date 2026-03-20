package com.paymentapp.core.exception.custom;

import com.paymentapp.core.exception.ErrorCode;

public class PlanException extends BusinessException {

    public PlanException(ErrorCode errorCode) {
        super(errorCode);
    }
}


