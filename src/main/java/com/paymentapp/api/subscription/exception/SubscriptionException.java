package com.paymentapp.api.subscription.exception;

import com.paymentapp.core.exception.ErrorCode;
import com.paymentapp.core.exception.custom.BusinessException;

public class SubscriptionException extends BusinessException {

    public SubscriptionException(ErrorCode errorCode) {
        super(errorCode);
    }
}


