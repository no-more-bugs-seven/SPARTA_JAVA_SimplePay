package com.paymentapp.api.auth.dto;

public record LoginResponse(
        AuthTokens tokens,
        UserInfoDto user
) {


    public record UserInfoDto(
            Long id,
            String username,
            String name,
            String profileImageUrl
    ) {

    }
}