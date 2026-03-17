package com.paymentapp.core.security.oauth2;

import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> registerUser(attributes));

        return new CustomOAuth2User(member.getId(), member.getEmail(), oAuth2User);
    }

    private Member registerUser(Map<String, Object> attributes) {

        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Entity 변환
        Member member = Member.builder()
                .email(email)
                .name(name)
                .build();

        return memberRepository.save(member);
    }
}