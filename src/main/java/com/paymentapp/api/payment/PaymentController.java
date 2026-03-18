package com.paymentapp.api.payment;

import com.paymentapp.api.payment.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 시도 기록 생성
     * @param request
     * @return
     */
    @PostMapping("/payments")
    public ResponseEntity<CreatePaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    /**
     * 결제 검증 조회 및 결제 확정
     * @param paymentId
     * @return
     */
    @PostMapping("/payments/{paymentId}/confirm")
    public ResponseEntity<ConfirmPaymentResponse> confirmPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.confirmPayment(paymentId));
    }

    @PostMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<CancelPaymentResponse> cancelPayment(
            @PathVariable String paymentId,
            @RequestBody CancelPaymentRequest request) {
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId, request));
    }
}
