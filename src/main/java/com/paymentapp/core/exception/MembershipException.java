package com.paymentapp.core.exception;

public class MembershipException extends BusinessException {

    public MembershipException(ErrorCode errorCode) {
        super(errorCode);
    }
}
