package com.paymentapp.core.exception.custom;

import com.paymentapp.core.exception.ErrorCode;

public class ProductException extends BusinessException {

    public ProductException(ErrorCode errorCode) {
        super(errorCode);
    }
}
