package com.paymentapp.api.auth;

import com.paymentapp.api.auth.dto.*;
import com.paymentapp.api.member.MemberService;
import com.paymentapp.core.annotation.LoginUser;
import com.paymentapp.core.constant.AuthConstants;
import com.paymentapp.core.dto.LoginUserInfoDto;
import com.paymentapp.api.member.exception.MemberErrorCode;
import com.paymentapp.api.member.exception.MemberException;
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
    public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest signUpRequest) {
        SignUpResponse response = authService.signUp(signUpRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 로그인 API
     * POST /api/auth/login
     *
     * 요청 본문:
     * {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     *
     * 응답 헤더:
     * Authorization: Bearer eyJhbGc...
     *
     * 응답 본문:
     * {
     *   "success": true,
     *   "email": "user@example.com"
     * }
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
     * 현재 로그인한 사용자 정보 조회 API
     * GET /api/auth/me
     *
     * 응답:
     * {
     *   "success": true,
     *   "email": "user@example.com",
     *   "customerUid": "CUST_xxxxx",
     *   "name": "홍길동"
     * }
     *
     * 중요: customerUid는 PortOne 빌링키 발급 시 활용!
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> getCurrentUser(@LoginUser LoginUserInfoDto loginUser) {
        // TODO: 구현
        // customerUid 생성은 조회 한 사용자 정보로 조합하여 생성, 추천 조합 : CUST_{userId}_{rand6:난수}
        return ResponseEntity.ok().body(memberService.getInfo(loginUser.id()));
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/reissue")
    public ResponseEntity<AuthTokens> reissue(
            @CookieValue(value = AuthConstants.REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new MemberException(MemberErrorCode.UNAUTHORIZED_ACCESS);
        }
        AuthTokens tokens = authService.reissue(refreshToken);
        // 쿠키를 클라이언트에 전송
        Cookie cookie = new Cookie(AuthConstants.ACCESS_TOKEN, tokens.accessToken());
        cookie.setPath(AuthConstants.COOKIE_PATH_ROOT);
        cookie.setMaxAge(AuthConstants.COOKIE_MAX_AGE_DEFAULT);
        response.addCookie(cookie);
        cookie = cookieUtils.createCookie(
                AuthConstants.REFRESH_TOKEN,
                tokens.refreshToken(),
                jwtTokenProvider.getRefreshTokenValidityInSeconds()
        );
        response.addCookie(cookie);
        return ResponseEntity.ok().header("Authorization", "Bearer " + tokens.accessToken()).body(tokens);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(value = AuthConstants.ACCESS_TOKEN, required = false) String accessToken,
            @CookieValue(value = AuthConstants.REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            authService.logout(accessToken, refreshToken);
        }
        Cookie cookie = cookieUtils.deleteCookie(AuthConstants.REFRESH_TOKEN);
        response.addCookie(cookie);
        return ResponseEntity.ok("{}");
    }
}