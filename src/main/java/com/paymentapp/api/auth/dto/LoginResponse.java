package com.paymentapp.api.auth.dto;

public record LoginResponse(
        boolean success,
        String email
) {
}