package com.paymentapp.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record AuthTokens(
        String accessToken,
        @JsonIgnore
        String refreshToken
) {}