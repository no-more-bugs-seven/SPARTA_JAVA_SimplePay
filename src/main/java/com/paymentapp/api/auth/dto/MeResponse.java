package com.paymentapp.api.auth.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record MeResponse(boolean success,
                         String email,
                         String customerUid,
                         String name,
                         String phone,
                         Long pointBalance
) {
}