package com.paymentapp.core.exception.custom;

import com.paymentapp.core.exception.ErrorCode;

public class SubscriptionException extends BusinessException {

    public SubscriptionException(ErrorCode errorCode) {
        super(errorCode);
    }
}


