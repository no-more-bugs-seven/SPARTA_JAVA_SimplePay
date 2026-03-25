package com.paymentapp.api.subscription.scheduler;

import com.paymentapp.api.subscription.SubscriptionRepository;
import com.paymentapp.api.subscription.SubscriptionService;
import com.paymentapp.api.subscription.entity.Subscription;
import com.paymentapp.api.subscription.enums.SubscriptionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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

    @Scheduled(cron = "0 * * * * *") // 매분 0초에 실행되도록
    @SchedulerLock(
            name = "subscription_auto_billing_lock", // 락의 고유명
            lockAtMostFor = "PT50S",  // 락 최대시간
            lockAtLeastFor = "PT10S"   // 락 최소시간
    )
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
                        subscription.getNextPaymentAt().plusMinutes(SubscriptionService.NEXT_PAYMENT_PERIOD)
                );
                log.info("구독 결제 성공: subscriptionId={}", subscription.getId());
            } catch (Exception e) {
                log.error("구독 결제 실패: subscriptionId={}, 사유={}", subscription.getId(), e.getMessage());
            }
        }
        log.info("정기 결제 스케줄러 실행 종료");
    }
}
