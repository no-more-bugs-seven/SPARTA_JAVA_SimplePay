package com.paymentapp.api.subscription;

import com.paymentapp.api.subscription.dto.*;
import com.paymentapp.core.annotation.LoginUser;
import com.paymentapp.core.dto.LoginUserInfoDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;


    /**
     * 구독 생성
     */
    @PostMapping
    public ResponseEntity<CreateSubscriptionResponse> createSubscription(
            @LoginUser LoginUserInfoDto loginUser,
            @Valid @RequestBody CreateSubscriptionRequest request
    ) {
        CreateSubscriptionResponse response = subscriptionService.createSubscription(
                loginUser.id(),
                request.customerUid(),
                request.planId(),
                request.billingKey(),
                request.amount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * 내 구독 단건 조회
     */
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> getMySubscription(
            @LoginUser LoginUserInfoDto loginUser,
            @PathVariable Long subscriptionId
    ) {
        SubscriptionResponse response = subscriptionService.getMySubscription(loginUser.id(), subscriptionId);
        return ResponseEntity.ok(response);
    }


    /**
     * 구독 해지
     */
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<UpdateSubscriptionResponse> cancelSubscription(
            @LoginUser LoginUserInfoDto loginUser,
            @PathVariable Long subscriptionId
    ) {
        UpdateSubscriptionResponse response = subscriptionService.cancelSubscription(loginUser.id(), subscriptionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 구독 플랜 변경
     */
    @PatchMapping("/{subscriptionId}/plan")
    public ResponseEntity<ChangeSubscriptionPlanResponse> changeSubscriptionPlan(
            @LoginUser LoginUserInfoDto loginUser,
            @PathVariable String subscriptionId,
            @Valid @RequestBody ChangeSubscriptionPlanRequest request
    ) {
        return ResponseEntity.ok(
                subscriptionService.changePlan(loginUser.id(), subscriptionId, request.planId())
        );
    }

}