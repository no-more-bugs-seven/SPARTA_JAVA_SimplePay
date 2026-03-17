package com.paymentapp.core.portone;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final RestClient restClient;

    @Value("${portone.api.secret-key}")
    private String secretKey;

    public PortOnePaymentResponse getPayment(String paymentId) {

        return restClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + secretKey)
                .retrieve()
                .body(PortOnePaymentResponse.class);
    }
}
