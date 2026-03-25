package com.paymentapp.api.payment.exception;

import com.paymentapp.core.exception.ErrorCode;
import com.paymentapp.core.exception.custom.BusinessException;

public class PaymentException extends BusinessException {

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
