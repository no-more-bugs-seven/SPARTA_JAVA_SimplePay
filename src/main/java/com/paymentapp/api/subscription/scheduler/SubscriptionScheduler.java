package com.paymentapp.api.subscription.scheduler;

import com.paymentapp.api.subscription.SubscriptionRepository;
import com.paymentapp.api.subscription.SubscriptionService;
import com.paymentapp.api.subscription.entity.Subscription;
import com.paymentapp.api.subscription.enums.SubscriptionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    /**
     * 매일 자정(00:00)에  결제를 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void runAutoBilling() {
        log.info("정기 결제 스케줄러 실행 시작");
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> paymentTargets = subscriptionRepository.findAllByStatusAndNextPaymentAtBefore(SubscriptionStatus.ACTIVE, now);

        log.info("결제 대상 건수: {}건", paymentTargets.size());

        for (Subscription subscription : paymentTargets) {
            try {
                subscriptionService.renewSubscription(
                        subscription.getId(),
                        subscription.getNextPaymentAt(),
                        subscription.getNextPaymentAt().plusMonths(1)
                );
                log.info("구독 결제 성공: subscriptionId={}", subscription.getId());
            } catch (Exception e) {
                log.error("구독 결제 실패: subscriptionId={}, 사유={}", subscription.getId(), e.getMessage());
            }
        }
        log.info("정기 결제 스케줄러 실행 종료");
    }
}
