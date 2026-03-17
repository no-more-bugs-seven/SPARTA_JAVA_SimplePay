package com.paymentapp.core.portone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PortOnePaymentResponse {

    private String paymentId;   // 우리가 보낸 paymentId
    private String status;      // PAID, FAILED 등
    private Double amount;        // 결제 금액
}
