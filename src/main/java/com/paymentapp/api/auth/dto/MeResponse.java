package com.paymentapp.api.auth.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
public record MeResponse(boolean success,
                         String email,
                         String customerUid,
                         String name,
                         String phone,
                         BigDecimal pointBalance
) {
}