package com.paymentapp.api.auth;

import com.paymentapp.api.auth.dto.*;
import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberService;
import com.paymentapp.core.exception.errorcode.MemberErrorCode;
import com.paymentapp.core.exception.custom.MemberException;
import com.paymentapp.core.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // MemberRepository 직접 의존 제거 → MemberService API를 통해서만 접근
    private final MemberService memberService;

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입 오케스트레이션
     * 실제 member 생성은 MemberService에 위임하고,
     * auth 도메인은 "가입 후 바로 로그인 처리"라는 흐름을 담당합니다.
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        memberService.createMember(request);
        return new SignUpResponse(true, "");
    }

    /**
     * 로그인
     * 회원 조회는 MemberService에 위임, 토큰 발급/저장은 auth 도메인이 담당
     */
    @Transactional
    public UserInfoDto login(LoginRequest request) {
        // MemberRepository 직접 사용 → MemberService 위임
        Member member = memberService.findByLoginId(request.email());

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.INVALID_CREDENTIALS);
        }

        // 토큰 발급
        AuthTokens tokens = generateTokens(member);

        // RefreshToken DB 저장/갱신 (auth 도메인의 책임)
        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        rt -> rt.updateToken(tokens.refreshToken()),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .memberId(member.getId())
                                        .token(tokens.refreshToken())
                                        .build()
                        )
                );

        return new UserInfoDto(tokens, new LoginResponse(true, member.getEmail()));
    }

    /**
     * 토큰 재발급 (RTR 패턴)
     */
    @Transactional
    public AuthTokens reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new MemberException(MemberErrorCode.UNAUTHORIZED_ACCESS);
        }

        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new MemberException(MemberErrorCode.UNAUTHORIZED_ACCESS));

        // MemberRepository 직접 사용 → MemberService 위임
        Member member = memberService.findById(tokenEntity.getMemberId());

        AuthTokens newTokens = generateTokens(member);
        tokenEntity.updateToken(newTokens.refreshToken());

        return newTokens;
    }

    /**
     * 로그아웃 - RefreshToken DB에서 삭제
     */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }

    // --- private ---

    private AuthTokens generateTokens(Member member) {
        String accessToken  = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail(), null);
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        return new AuthTokens(accessToken, refreshToken);
    }
}