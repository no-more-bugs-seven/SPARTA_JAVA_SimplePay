package com.paymentapp.core.util;


import com.paymentapp.api.member.Member;
import com.paymentapp.api.member.MemberRepository;
import com.paymentapp.api.product.Product;
import com.paymentapp.api.product.ProductRepository;
import com.paymentapp.core.constant.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataInit implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
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
                    .pointBalance(1000L)
                    .build();

            Member savedMember = memberRepository.save(member);
        }


        List<Product> products = List.of(
                new Product("소음기 장착 권총", new BigDecimal("1000.00"), 5,
                        "불법 개조된 소음기 권총. 추적 회피 기능 포함.",
                        ProductStatus.ON_SALE, "WEAPON"),

                new Product("밀리터리 전술 나이프", new BigDecimal("1100.00"), 12,
                        "군용 나이프 개조형. 내구성 강화.",
                        ProductStatus.ON_SALE, "WEAPON"),

                new Product("개조 감시 드론", new BigDecimal("1200.00"), 3,
                        "고성능 카메라 장착. 야간 촬영 가능.",
                        ProductStatus.ON_SALE, "EQUIPMENT"),

                new Product("고성능 도청 장치", new BigDecimal("1300.00"), 8,
                        "원거리 음성 수집 장비.",
                        ProductStatus.ON_SALE, "EQUIPMENT"),

                new Product("롤렉스 레플리카 시계", new BigDecimal("1400.00"), 20,
                        "정품과 구분 어려운 A급 짝퉁.",
                        ProductStatus.ON_SALE, "FAKE_LUXURY"),

                new Product("루이비통 레플리카 가방", new BigDecimal("1500.00"), 15,
                        "고급 레플리카 가방.",
                        ProductStatus.ON_SALE, "FAKE_LUXURY"),

                new Product("디자이너 짝퉁 의류 세트", new BigDecimal("1600.00"), 10,
                        "브랜드 로고 포함 고퀄 의류.",
                        ProductStatus.ON_SALE, "FAKE_LUXURY"),

                new Product("한정판 스니커즈 (리셀)", new BigDecimal("1700.00"), 4,
                        "리셀가 폭등한 희귀 모델.",
                        ProductStatus.ON_SALE, "RARE_ITEM"),

                new Product("VIP 콘서트 암표", new BigDecimal("1800.00"), 6,
                        "정가 대비 5배 프리미엄 좌석.",
                        ProductStatus.ON_SALE, "TICKET"),

                new Product("스포츠 결승전 암표", new BigDecimal("1900.00"), 5,
                        "결승전 VIP 좌석.",
                        ProductStatus.ON_SALE, "TICKET"),

                new Product("도난된 왕실 황금 잔", new BigDecimal("2000.00"), 1,
                        "왕실에서 도난된 전설급 유물.",
                        ProductStatus.ON_SALE, "ARTIFACT"),

                new Product("미공개 유명 화가 유화", new BigDecimal("2100.00"), 1,
                        "출처 불명의 미공개 작품.",
                        ProductStatus.ON_SALE, "ART"),

                new Product("고대 청동 불상", new BigDecimal("2200.00"), 2,
                        "밀반출된 문화재.",
                        ProductStatus.ON_SALE, "ARTIFACT"),

                new Product("박물관 유출 보석 목걸이", new BigDecimal("2300.00"), 1,
                        "박물관에서 사라진 고가 보석.",
                        ProductStatus.ON_SALE, "JEWEL"),

                new Product("고서적 원본 필사본", new BigDecimal("2400.00"), 2,
                        "현존 유일 필사본.",
                        ProductStatus.ON_SALE, "RARE_ITEM"),

                new Product("위조 신분증 세트", new BigDecimal("2500.00"), 25,
                        "고급 위조 신분증 패키지.",
                        ProductStatus.ON_SALE, "FORGERY"),

                new Product("프리미엄 해킹 툴킷", new BigDecimal("2600.00"), 7,
                        "보안 우회용 소프트웨어 패키지.",
                        ProductStatus.ON_SALE, "DIGITAL"),

                new Product("암호화 통신 장비", new BigDecimal("2700.00"), 6,
                        "추적 불가능한 통신 장치.",
                        ProductStatus.ON_SALE, "EQUIPMENT"),

                new Product("희귀 빈티지 시계", new BigDecimal("2800.00"), 2,
                        "1970년대 생산된 희귀 모델.",
                        ProductStatus.ON_SALE, "RARE_ITEM"),

                new Product("한정판 명품 시계 (짝퉁)", new BigDecimal("2900.00"), 14,
                        "고급 레플리카 시계.",
                        ProductStatus.ON_SALE, "FAKE_LUXURY")
        );
        productRepository.saveAll(products);
    }
}