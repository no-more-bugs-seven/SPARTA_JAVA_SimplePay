package com.paymentapp.core.portone;

import com.paymentapp.core.portone.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Slf4j
@Component
public class PortOneClient {

    private final RestClient restClient;

    @Value("${portone.store.id}")
    private String storeId;

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
                    .uri("/payments/{paymentId}?storeId=" + storeId, paymentId)
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
                    .storeId(storeId)
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


    /**
     * 빌링키 검증
     */
    public boolean validateBillingKey(String billingKey) {
        try {
            log.info("PortOne 빌링키 검증 요청 billingKey={}", billingKey);

            PortOneBillingKeyInfoResponse response = restClient.get()
                    .uri("billing-keys/{billingKey}?storeId=" + storeId, billingKey)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        log.warn("포트원에 존재하지 않는 빌링키입니다. billingKey={}", billingKey);
                        throw new RuntimeException("존재하지 않는 빌링키");
                    })
                    .body(PortOneBillingKeyInfoResponse.class);

            if (response != null && "ISSUED".equals(response.status())) {
                log.info("PortOne 빌링키 검증 성공! (상태: ISSUED)");
                return true;
            } else {
                log.warn("유효하지 않은 빌링키 상태입니다. (상태: {})", response != null ? response.status() : "null");
                return false;
            }

        } catch (Exception e) {
            log.error("PortOne 빌링키 검증 중 에러 발생", e);
            return false;
        }
    }

    /**
     * 결제 with 빌링키
     */
    public PortOneBillingPaymentResponse payWithBillingKey(
            String billingKey,
            BigDecimal amount,
            String uniquePaymentId
    ) {
        try {
            log.info("PortOne 빌링키 결제 요청 uniquePaymentId={}, amount={}", uniquePaymentId, amount);

            PortOneBillingPaymentRequest requestBody = new PortOneBillingPaymentRequest(
                    storeId,
                    billingKey,
                    "정기 구독 결제",
                    "KRW",
                    new PortOneBillingPaymentRequest.Amount(amount.intValue())
            );
            PortOneBillingPaymentResponse responseBody = restClient.post()
                    .uri("/payments/{paymentId}/billing-key", uniquePaymentId)
                    .body(requestBody)
                    .retrieve()
                    .body(PortOneBillingPaymentResponse.class);

            if (responseBody != null) {
                responseBody.handleSuccess(uniquePaymentId);
                log.info("PortOne 빌링키 결제 성공! pgTxId={}, paidAt={}", responseBody.getPayment().getPgTxId(), responseBody.getPayment().getPaidAt());

                return responseBody;
            } else {
                return new PortOneBillingPaymentResponse(false, uniquePaymentId, "null");
            }

        } catch (RestClientResponseException e) {
            String rawErrorBody = e.getResponseBodyAsString();
            log.error("포트원 찐 원본 에러 응답: {}", rawErrorBody);
            try {
                PortOneBillingPaymentResponse errorResponse = e.getResponseBodyAs(PortOneBillingPaymentResponse.class);

                String errMsg = (errorResponse != null && errorResponse.getErrorMessage() != null)
                        ? errorResponse.getErrorMessage()
                        : "결제 승인 거절 (상세 사유 없음)";

                log.warn("PortOne 결제 실패 (API 응답): {}", errMsg);

                return new PortOneBillingPaymentResponse(false, uniquePaymentId, errMsg);

            } catch (Exception parseException) {
                log.error("PortOne 에러 메시지 파싱 실패", parseException);
                return new PortOneBillingPaymentResponse(false, uniquePaymentId, "결제 처리 중 에러 발생");
            }

        } catch (Exception e) {
            log.error("PortOne 통신 오류 uniquePaymentId={}", uniquePaymentId, e);
            return new PortOneBillingPaymentResponse(false, uniquePaymentId, "결제 서버 통신 오류");
        }
    }


}
