package com.paymentapp.api.subscription;

import com.paymentapp.api.subscription.dto.ChangeSubscriptionPlanRequest;
import com.paymentapp.api.subscription.dto.CreateSubscriptionRequest;
import com.paymentapp.api.subscription.dto.CreateSubscriptionResponse;
import com.paymentapp.api.subscription.dto.SubscriptionResponse;
import com.paymentapp.api.subscription.dto.UpdateSubscriptionRequest;
import com.paymentapp.api.subscription.dto.UpdateSubscriptionResponse;
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
                request.billingKey()
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
        subscriptionService.cancelSubscription(loginUser.id(), subscriptionId);

        return ResponseEntity.ok(
                new UpdateSubscriptionResponse(true, subscriptionId)
        );
    }

}