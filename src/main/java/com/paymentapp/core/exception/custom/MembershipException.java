package com.paymentapp.core.exception.custom;

import com.paymentapp.core.exception.ErrorCode;

public class MembershipException extends BusinessException {

    public MembershipException(ErrorCode errorCode) {
        super(errorCode);
    }
}
