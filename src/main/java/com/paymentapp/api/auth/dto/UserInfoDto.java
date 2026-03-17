package com.paymentapp.api.auth.dto;

public record UserInfoDto(
        AuthTokens tokens,
        LoginResponse response
) {

}
