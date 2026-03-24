package com.paymentapp.core.util;


import com.paymentapp.api.plan.entity.BillingCycle;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataInit implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
            insertMembershipTiers();
            insertMembers();
            insertMembershipHistories();
            insertProducts();
            insertPlans();
    }

    /**
     * 멤버십 등급 마스터 데이터 초기화
     *   - Normal : 0       원 이상 (= 모든 신규 가입자 기본 등급)
     *   - VIP    : 50,001  원 이상 (= 5만 원 초과 시 승급)
     *   - VVIP   : 150,000 원 이상 (= 15만 원 이상 시 승급)
     */

    private void insertMembershipTiers() {
        Long tierCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM membership_tiers", Long.class);
        if (tierCount != null && tierCount > 0) {
            return;
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        List<Object[]> batchArgs = List.of(
                // name,    min_spent_amount, point_rate, created_at, modified_at
                new Object[]{"Normal", new BigDecimal("0"),       new BigDecimal("0.01"), now, now},
                new Object[]{"VIP",    new BigDecimal("50001"),   new BigDecimal("0.05"), now, now},
                new Object[]{"VVIP",   new BigDecimal("150000"),  new BigDecimal("0.10"), now, now}
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO membership_tiers (name, min_spent_amount, point_rate, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?)",
                batchArgs
        );
    }

    private void insertMembers() {
        Long memberCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        if (memberCount != null && memberCount > 0) {
            return;
        }

        Long normalTierId = jdbcTemplate.queryForObject(
                "SELECT id FROM membership_tiers WHERE min_spent_amount = 0",
                Long.class
        );

        List<String> usernames = List.of("kuromi", "mamel", "pikachu", "kitty", "heartping");
        List<String> names = List.of("고은지", "김소현", "박영수", "성기찬", "이지민");
        String encodedPassword = passwordEncoder.encode("abc1234!");

        List<Object[]> batchArgs = new java.util.ArrayList<>();
        for (int i = 0; i < usernames.size(); i++) {
            batchArgs.add(new Object[]{
                    encodedPassword,
                    names.get(i),
                    usernames.get(i) + "@test.com",
                    "010-1234-567" + i,
                    1000L,
                    normalTierId
            });
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO users (password, name, email, phone, point_balance, membership_tier_id) VALUES (?, ?, ?, ?, ?, ?)",
                batchArgs
        );
    }

    /**
     * 테스트 유저들의 초기 멤버십 히스토리 생성
     */
    private void insertMembershipHistories() {
        Long historyCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM membership_histories", Long.class);
        if (historyCount != null && historyCount > 0) {
            return;
        }

        Long normalTierId = jdbcTemplate.queryForObject(
                "SELECT id FROM membership_tiers WHERE min_spent_amount = 0",
                Long.class
        );

        List<Long> userIds = jdbcTemplate.queryForList("SELECT id FROM users", Long.class);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        List<Object[]> batchArgs = new java.util.ArrayList<>();
        for (Long userId : userIds) {
            batchArgs.add(new Object[]{userId, normalTierId, now, now});
        }
        // changed_at 엔티티에 없어서 뺏음
        jdbcTemplate.batchUpdate(
                "INSERT INTO membership_histories (user_id, tier_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?)",
                batchArgs
        );
    }

    private void insertProducts() {
        Long productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Long.class);
        if (productCount != null && productCount > 0) {
            return;
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        List<Object[]> batchArgs = List.of(
                new Object[]{"소음기 장착 권총", new BigDecimal("1000.00"), 5, "불법 개조된 소음기 권총. 추적 회피 기능 포함.", "ON_SALE", "WEAPON", now, now},
                new Object[]{"밀리터리 전술 나이프", new BigDecimal("1100.00"), 12, "군용 나이프 개조형. 내구성 강화.", "ON_SALE", "WEAPON", now, now},
                new Object[]{"개조 감시 드론", new BigDecimal("1200.00"), 3, "고성능 카메라 장착. 야간 촬영 가능.", "ON_SALE", "EQUIPMENT", now, now},
                new Object[]{"고성능 도청 장치", new BigDecimal("1300.00"), 8, "원거리 음성 수집 장비.", "ON_SALE", "EQUIPMENT", now, now},
                new Object[]{"롤렉스 레플리카 시계", new BigDecimal("1400.00"), 20, "정품과 구분 어려운 A급 짝퉁.", "ON_SALE", "FAKE_LUXURY", now, now},
                new Object[]{"루이비통 레플리카 가방", new BigDecimal("1500.00"), 15, "고급 레플리카 가방.", "ON_SALE", "FAKE_LUXURY", now, now},
                new Object[]{"비공개된 명품", new BigDecimal("1600.00"), 10, "공개되지 않은 명품.", "ON_SALE", "FAKE_LUXURY", now, now},
                new Object[]{"한정판 스니커즈 (리셀)", new BigDecimal("1700.00"), 4, "리셀가 폭등한 희귀 모델.", "ON_SALE", "RARE_ITEM", now, now},
                new Object[]{"VIP 콘서트 암표", new BigDecimal("1800.00"), 6, "정가 대비 5배 프리미엄 좌석.", "ON_SALE", "TICKET", now, now},
                new Object[]{"스포츠 결승전 암표", new BigDecimal("1900.00"), 5, "결승전 VIP 좌석.", "ON_SALE", "TICKET", now, now},
                new Object[]{"도난된 왕실 황금 잔", new BigDecimal("2000.00"), 1, "왕실에서 도난된 전설급 유물.", "ON_SALE", "ARTIFACT", now, now},
                new Object[]{"미공개 유명 화가 유화", new BigDecimal("2100.00"), 1, "출처 불명의 미공개 작품.", "ON_SALE", "ART", now, now},
                new Object[]{"고대 청동 불상", new BigDecimal("2200.00"), 2, "밀반출된 문화재.", "ON_SALE", "ARTIFACT", now, now},
                new Object[]{"박물관 유출 보석 목걸이", new BigDecimal("2300.00"), 1, "박물관에서 사라진 고가 보석.", "ON_SALE", "JEWEL", now, now},
                new Object[]{"고서적 원본 필사본", new BigDecimal("2400.00"), 2, "현존 유일 필사본.", "ON_SALE", "RARE_ITEM", now, now},
                new Object[]{"위조 신분증 세트", new BigDecimal("2500.00"), 25, "고급 위조 신분증 패키지.", "ON_SALE", "FORGERY", now, now},
                new Object[]{"프리미엄 해킹 툴킷", new BigDecimal("2600.00"), 7, "보안 우회용 소프트웨어 패키지.", "ON_SALE", "DIGITAL", now, now},
                new Object[]{"암호화 통신 장비", new BigDecimal("2700.00"), 6, "추적 불가능한 통신 장치.", "ON_SALE", "EQUIPMENT", now, now},
                new Object[]{"희귀 빈티지 시계", new BigDecimal("2800.00"), 2, "1970년대 생산된 희귀 모델.", "ON_SALE", "RARE_ITEM", now, now},
                new Object[]{"한정판 명품 시계", new BigDecimal("2900.00"), 14, "고급스러운 디자인의 패션 시계", "ON_SALE", "FAKE_LUXURY", now, now}
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO products (name, price, stock, description, status, category, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                batchArgs
        );
    }

    private void insertPlans() {
        Long planCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM plans", Long.class);
        if (planCount != null && planCount > 0) {
            return;
        }

        List<Object[]> batchArgs = List.of(
                new Object[]{"NOOB", "입문자", new BigDecimal("9900"), BillingCycle.MONTHLY.name(), true},
                new Object[]{"BROKER", "중개인", new BigDecimal("19900"), BillingCycle.MONTHLY.name(), true},
                new Object[]{"BLACK_HAND", "검은손", new BigDecimal("29900"), BillingCycle.MONTHLY.name(), true}
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO plans (plan_id, name, amount, billing_cycle, active) " +
                        "VALUES (?, ?, ?, ?, ?)",
                batchArgs
        );
    }
}