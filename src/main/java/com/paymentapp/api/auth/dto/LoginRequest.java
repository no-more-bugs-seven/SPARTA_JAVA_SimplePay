package com.paymentapp.api.auth.dto;

public record LoginRequest(
        String email,

        String password
) {
}