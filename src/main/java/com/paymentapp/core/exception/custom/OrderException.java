package com.paymentapp.core.exception.custom;

import com.paymentapp.core.exception.ErrorCode;

public class OrderException extends BusinessException {

    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
