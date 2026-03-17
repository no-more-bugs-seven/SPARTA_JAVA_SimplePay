package com.paymentapp.core.util;


import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataInit implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (memberRepository.count() > 0) return;

        List<String> usernames = List.of("kuromi", "mamel", "pikachu", "kitty", "heartping");
        List<String> names = List.of("고은지", "김소현", "박영수", "성기찬", "이지민");
        String password = passwordEncoder.encode("abc1234!");

        for (int i = 0; i < usernames.size(); i++) {
            Member member = Member.builder()
                    .password(password)
                    .name(names.get(i))
                    .email(usernames.get(i) + "@test.com")
                    .phone("010-1234-567" + i)
                    .build();

            Member savedMember = memberRepository.save(member);
        }
    }
}