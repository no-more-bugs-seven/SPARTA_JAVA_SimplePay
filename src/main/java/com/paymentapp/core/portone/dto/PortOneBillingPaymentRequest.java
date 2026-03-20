package com.paymentapp.core.portone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PortOneBillingPaymentRequest {
    private String storeId;
    private String billingKey;
    private String orderName;
    private String currency;
    private Amount amount;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        private long total;
    }
}