package com.paymentapp.core.portone.dto;

import lombok.Builder;

@Builder
public class PortOneCancelRequest {
    private String reason;
}
