package com.paymentapp.core.portone;

import com.paymentapp.core.portone.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final RestClient restClient;

    @Value("${portone.api.secret}")
    private String secretKey;

    public PortOnePaymentResponse getPayment(String paymentId) {

        try {
            log.info("PortOne 요청 paymentId={}", paymentId);
            log.info("secretKey={}", secretKey);

            PortOnePaymentResponse response = restClient.get()
                    .uri("/payments/{paymentId}", paymentId)
                    .header("Authorization", "Bearer " + secretKey)
                    .retrieve()
                    .body(PortOnePaymentResponse.class);

            log.info("PortOne 응답={}", response);

            return response;

        } catch (Exception e) {
            log.error("PortOne API 호출 실패", e);
            throw new RuntimeException("PortOne 결제 조회 실패", e);
        }
    }
}
