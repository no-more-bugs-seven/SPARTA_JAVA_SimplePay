package com.paymentapp.api.auth;

import com.paymentapp.api.auth.dto.*;
import com.paymentapp.api.member.MemberService;
import com.paymentapp.core.constant.AuthConstants;
import com.paymentapp.core.dto.ApiResponse;
import com.paymentapp.core.exception.MemberErrorCode;
import com.paymentapp.core.exception.MemberException;
import com.paymentapp.core.security.jwt.JwtTokenProvider;
import com.paymentapp.core.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    // signUp/login/reissue/logout → AuthService가 모든 인증 흐름 담당
    private final AuthService authService;
    // checkDuplicate는 member 도메인 기능 → MemberService 직접 호출 (auth 흐름 아님)
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;

    /**
     * 회원가입 — 인증 도메인(AuthService)이 오케스트레이션
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody @Valid SignUpRequest signUpRequest) {
        SignUpResponse response = authService.signUp(signUpRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 중복 확인 — member 도메인 기능이므로 MemberService 직접 호출
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkDuplicate(
            @RequestParam String type, @RequestParam String value) {
        boolean isAvailable = memberService.checkDuplicate(type, value);
        String message = isAvailable ? "사용 가능한 " + type + "입니다." : "이미 사용 중인 " + type + "입니다.";
        DuplicateCheckResponse response = isAvailable
                ? DuplicateCheckResponse.available(message)
                : DuplicateCheckResponse.unavailable(message);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletResponse response) {

        UserInfoDto dto = authService.login(loginRequest);
        Cookie cookie = cookieUtils.createCookie(
                AuthConstants.REFRESH_TOKEN,
                dto.tokens().refreshToken(),
                jwtTokenProvider.getRefreshTokenValidityInSeconds()
        );

        response.addCookie(cookie);
        return ResponseEntity.ok().header("Authorization", "Bearer " + dto.tokens().accessToken()).body(dto.response());
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthTokens>> reissue(
            @CookieValue(value = AuthConstants.REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new MemberException(MemberErrorCode.UNAUTHORIZED_ACCESS);
        }
        AuthTokens tokens = authService.reissue(refreshToken);
        Cookie cookie = cookieUtils.createCookie(
                AuthConstants.REFRESH_TOKEN,
                tokens.refreshToken(),
                jwtTokenProvider.getRefreshTokenValidityInSeconds()
        );
        response.addCookie(cookie);
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @CookieValue(value = AuthConstants.REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        Cookie cookie = cookieUtils.deleteCookie(AuthConstants.REFRESH_TOKEN);
        response.addCookie(cookie);
        return ResponseEntity.ok(ApiResponse.success(AuthConstants.LOGOUT_SUCCESS_MESSAGE));
    }
}