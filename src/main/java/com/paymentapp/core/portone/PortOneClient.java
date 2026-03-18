package com.paymentapp.core.portone;

import com.paymentapp.core.portone.dto.PortOneCancelRequest;
import com.paymentapp.core.portone.dto.PortOneCancelResponse;
import com.paymentapp.core.portone.dto.PortOnePaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class PortOneClient {

    private final RestClient restClient;

    public PortOneClient(
            @Value("${portone.api.base-url}") String baseUrl,
            @Value("${portone.api.secret}") String apiSecret
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "PortOne " + apiSecret)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public PortOnePaymentResponse getPayment(String paymentId) {
        try {
            log.info("PortOne 요청 paymentId={}", paymentId);

            return restClient.get()
                    .uri("/payments/{paymentId}", paymentId)
                    .retrieve()
                    // 401 에러(인증 실패) 발생 시 로그 출력
                    .onStatus(status -> status.value() == 401, (request, response) -> {
                        log.error("V2 인증 실패: 시크릿 키가 틀렸거나 'PortOne ' 접두사가 누락됨");
                        throw new RuntimeException("PortOne V2 인증 실패");
                    })
                    // 404 에러(결제 건 없음) 발생 시
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        log.error("결제 건을 찾을 수 없음: paymentId={}", paymentId);
                        throw new RuntimeException("존재하지 않는 결제 ID");
                    })
                    .body(PortOnePaymentResponse.class);

        } catch (Exception e) {
            log.error("PortOne API 호출 실패", e);
            throw new RuntimeException("PortOne 결제 조회 실패", e);
        }
    }

    public PortOneCancelResponse cancelPayment(String paymentId, String reason) {
        try {
            log.info("PortOne 환불 요청 paymentId={}", paymentId);

            PortOneCancelRequest request = PortOneCancelRequest.builder()
                    .reason(reason)
                    .build();

            PortOneCancelResponse response = restClient.post()
                    .uri("/payments/{paymentId}/cancel", paymentId)
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.value() == 401, (req, res) -> {
                        throw new RuntimeException("인증 실패: Secret Key를 확인하세요.");
                    })
                    .body(PortOneCancelResponse.class);

            log.info("PortOne 환불 성공 paymentId={}, status={}",
                    response.getPaymentId(), response.getStatus());

            return response;

        } catch (Exception e) {
            log.error("PortOne 환불 실패 paymentId={}", paymentId, e);
            throw new RuntimeException("PortOne 환불 API 호출 실패", e);
        }
    }
}
