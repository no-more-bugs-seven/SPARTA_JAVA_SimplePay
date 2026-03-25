package com.paymentapp.api.member;

import com.paymentapp.api.auth.dto.MeResponse;
import com.paymentapp.api.auth.dto.SignUpRequest;
import com.paymentapp.api.membership.MembershipService;
import com.paymentapp.api.member.exception.MemberErrorCode;
import com.paymentapp.api.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Lazy
    @Autowired
    private final MembershipService membershipService;

    /**
     * 회원 생성 (auth 도메인에서 회원가입 시 위임받아 실행)
     * 중복 검증 + 비밀번호 암호화 + 저장 처리
     */
    @Transactional
    public Member createMember(SignUpRequest signUpRequest) {
        String email = signUpRequest.email();
        String phone = signUpRequest.phone();

        // 이메일/전화번호 중복체크
        if (email != null && memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL);
        }
        if (phone != null && memberRepository.existsByPhone(phone)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_PHONE);
        }

        Member member = Member.builder()
                .password(passwordEncoder.encode(signUpRequest.password()))
                .email(email)
                .phone(phone)
                .name(signUpRequest.name())
                .pointBalance(BigDecimal.ZERO)
                .membershipTier(membershipService.getNormalTier())
                .build();
        Member saved = memberRepository.save(member);
        membershipService.initMembership(saved);
        return saved;
    }

    /**
     * 로그인 ID(이메일/전화번호/유저네임) 기반 회원 조회 (auth 도메인에서 로그인 시 위임)
     */
    public Member findByLoginId(String loginId) {
        if (loginId.contains("@")) {
            return memberRepository.findByEmail(loginId)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.INVALID_CREDENTIALS));
        } else if (loginId.matches("^[0-9\\-]+$")) {
            return memberRepository.findByPhone(loginId)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.INVALID_CREDENTIALS));
        } else {
            throw new MemberException(MemberErrorCode.INVALID_CREDENTIALS);
        }
    }

    /**
     * ID 기반 회원 조회
     */
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public MeResponse getInfo(Long memberId) {
        Member member = findById(memberId);
        return MeResponse.builder().
                success(true).
                email(member.getEmail()).
                customerUid("CUST_" + Math.abs(member.getEmail().hashCode())).
                name(member.getName()).
                phone(member.getPhone()).
                pointBalance(member.getPointBalance()).
                build();
    }

    /**
     * DB I/O 없이 연관관계 설정용 Proxy 객체만 필요할 때 (post 도메인에서 위임)
     */
    public Member getReferenceById(Long memberId) {
        return memberRepository.getReferenceById(memberId);
    }
}