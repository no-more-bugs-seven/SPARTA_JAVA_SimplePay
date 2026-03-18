package com.paymentapp.core.exception.custom;

import com.paymentapp.core.exception.ErrorCode;

public class PaymentException extends BusinessException {

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
