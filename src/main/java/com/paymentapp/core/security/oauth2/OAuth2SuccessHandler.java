package com.paymentapp.core.security.oauth2;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentapp.api.auth.dto.AuthTokens;
import com.paymentapp.core.constant.AuthConstants;
import com.paymentapp.core.security.jwt.JwtTokenProvider;
import com.paymentapp.core.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();

        Long id = principal.getId();
        String email = principal.getEmail();

        String accessToken = jwtTokenProvider.createAccessToken(id, email, null);
        String refreshToken = jwtTokenProvider.createRefreshToken(id);


        // 쿠키를 클라이언트에 전송
        Cookie cookie = new Cookie(AuthConstants.ACCESS_TOKEN, accessToken);
        cookie.setPath(AuthConstants.COOKIE_PATH_ROOT);
        cookie.setMaxAge(AuthConstants.COOKIE_MAX_AGE_DEFAULT);
        response.addCookie(cookie);
        response.addCookie(cookieUtils.createCookie(
                AuthConstants.REFRESH_TOKEN,
                refreshToken,
                AuthConstants.COOKIE_MAX_AGE_DEFAULT
        ));
        response.sendRedirect("/");
    }
}