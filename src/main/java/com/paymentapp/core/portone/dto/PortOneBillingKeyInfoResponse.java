package com.paymentapp.core.portone.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneBillingKeyInfoResponse(
        String status,
        String billingKey,
        String storeId,
        String issuedAt,
        String deletedAt
) {
}