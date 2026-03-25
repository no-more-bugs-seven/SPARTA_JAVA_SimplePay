package com.paymentapp.api.plan.exception;

import com.paymentapp.core.exception.ErrorCode;
import com.paymentapp.core.exception.custom.BusinessException;

public class PlanException extends BusinessException {

    public PlanException(ErrorCode errorCode) {
        super(errorCode);
    }
}


