package com.paymentapp.api.product.exception;

import com.paymentapp.core.exception.ErrorCode;
import com.paymentapp.core.exception.custom.BusinessException;

public class ProductException extends BusinessException {

    public ProductException(ErrorCode errorCode) {
        super(errorCode);
    }
}
