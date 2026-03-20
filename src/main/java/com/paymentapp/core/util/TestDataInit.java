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
                new Object[]{"프리미엄 무선 이어폰", new BigDecimal("1000.00"), 5, "고음질 블루투스 이어폰", "ON_SALE", "ELECTRONICS", now, now},
                new Object[]{"기계식 키보드", new BigDecimal("1100.00"), 12, "타건감이 좋은 기계식 키보드", "ON_SALE", "ELECTRONICS", now, now},
                new Object[]{"스마트 홈 카메라", new BigDecimal("1200.00"), 3, "실내 모니터링용 카메라", "ON_SALE", "HOME", now, now},
                new Object[]{"휴대용 블루투스 스피커", new BigDecimal("1300.00"), 8, "야외에서도 쓰기 좋은 스피커", "ON_SALE", "ELECTRONICS", now, now},
                new Object[]{"프리미엄 손목시계", new BigDecimal("1400.00"), 20, "심플한 디자인의 패션 시계", "ON_SALE", "FASHION", now, now},
                new Object[]{"가죽 숄더백", new BigDecimal("1500.00"), 15, "데일리용 고급 가죽 가방", "ON_SALE", "FASHION", now, now},
                new Object[]{"디자이너 의류 세트", new BigDecimal("1600.00"), 10, "계절용 코디 세트", "ON_SALE", "FASHION", now, now},
                new Object[]{"한정판 스니커즈", new BigDecimal("1700.00"), 4, "수량 한정 인기 모델", "ON_SALE", "SHOES", now, now},
                new Object[]{"VIP 콘서트 패키지", new BigDecimal("1800.00"), 6, "공식 공연 관람 패키지", "ON_SALE", "TICKET", now, now},
                new Object[]{"스포츠 결승전 티켓", new BigDecimal("1900.00"), 5, "결승전 관람권", "ON_SALE", "TICKET", now, now},
                new Object[]{"프리미엄 골드 컵", new BigDecimal("2000.00"), 1, "기념 장식용 골드 컵", "ON_SALE", "COLLECTIBLE", now, now},
                new Object[]{"유명 화가 아트 포스터", new BigDecimal("2100.00"), 1, "인테리어용 고급 포스터", "ON_SALE", "ART", now, now},
                new Object[]{"청동 장식상", new BigDecimal("2200.00"), 2, "고풍스러운 인테리어 소품", "ON_SALE", "ART", now, now},
                new Object[]{"보석 목걸이", new BigDecimal("2300.00"), 1, "포인트 주얼리 목걸이", "ON_SALE", "JEWEL", now, now},
                new Object[]{"고서 복각본", new BigDecimal("2400.00"), 2, "클래식 감성의 수집용 도서", "ON_SALE", "BOOK", now, now},
                new Object[]{"프리미엄 문구 세트", new BigDecimal("2500.00"), 25, "비즈니스용 문구 패키지", "ON_SALE", "STATIONERY", now, now},
                new Object[]{"개발자 생산성 툴킷", new BigDecimal("2600.00"), 7, "업무 효율 향상 소프트웨어 번들", "ON_SALE", "DIGITAL", now, now},
                new Object[]{"스마트 통신 기기", new BigDecimal("2700.00"), 6, "휴대용 통신 디바이스", "ON_SALE", "ELECTRONICS", now, now},
                new Object[]{"희귀 빈티지 시계", new BigDecimal("2800.00"), 2, "수집가용 빈티지 모델", "ON_SALE", "COLLECTIBLE", now, now},
                new Object[]{"한정판 명품 스타일 시계", new BigDecimal("2900.00"), 14, "고급스러운 디자인의 패션 시계", "ON_SALE", "FASHION", now, now}
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