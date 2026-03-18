package com.paymentapp.api.point;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointExpirationScheduler {

    private final PointService pointService;

    // 매일 자정 만료 포인트 소멸 처리
    @Scheduled(cron = "0 0 0 * * *")
    public void expirePoints() {
        pointService.expirePoints();
    }
}