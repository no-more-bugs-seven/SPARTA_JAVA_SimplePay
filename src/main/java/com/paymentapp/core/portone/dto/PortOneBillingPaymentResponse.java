package com.paymentapp.core.portone.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortOneBillingPaymentResponse {
    private boolean success;
    private String paymentId;

    @JsonProperty("message")
    private String errorMessage;
    private BillingKeyPaymentSummary payment;

    public PortOneBillingPaymentResponse(boolean success, String paymentId, String errorMessage) {
        this.success = success;
        this.paymentId = paymentId;
        this.errorMessage = errorMessage;
    }

    public void handleSuccess(String paymentId) {
        this.success = true;
        this.paymentId = paymentId;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BillingKeyPaymentSummary {
        private String pgTxId;
        private String paidAt;
    }
}