package com.paymentapp.api.membership.exception;

import com.paymentapp.core.exception.ErrorCode;
import com.paymentapp.core.exception.custom.BusinessException;

public class MembershipException extends BusinessException {

    public MembershipException(ErrorCode errorCode) {
        super(errorCode);
    }
}
