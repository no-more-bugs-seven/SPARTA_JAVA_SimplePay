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

    @PostMapping
    public ResponseEntity<CreateSubscriptionResponse> createSubscription(
            @LoginUser LoginUserInfoDto loginUser,
            @Valid @RequestBody CreateSubscriptionRequest request
    ) {
        CreateSubscriptionResponse response = subscriptionService.create(
                loginUser.id(),
                request.customerUid(),
                request.planId(),
                request.billingKey(),
                request.amount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @LoginUser LoginUserInfoDto loginUser,
            @PathVariable String subscriptionId
    ) {
        return ResponseEntity.ok(
                subscriptionService.getSubscription(loginUser.id(), subscriptionId)
        );
    }

    @PatchMapping("/{subscriptionId}")
    public ResponseEntity<UpdateSubscriptionResponse> updateSubscription(
            @LoginUser LoginUserInfoDto loginUser,
            @PathVariable String subscriptionId,
            @RequestBody UpdateSubscriptionRequest request
    ) {
        if ("cancel".equalsIgnoreCase(request.action())) {
            return ResponseEntity.ok(
                    subscriptionService.cancel(loginUser.id(), subscriptionId)
            );
        }

        throw new IllegalArgumentException("지원하지 않는 action 입니다.");
    }

    @PatchMapping("/{subscriptionId}/plan")
    public ResponseEntity<SubscriptionResponse> changeSubscriptionPlan(
            @LoginUser LoginUserInfoDto loginUser,
            @PathVariable String subscriptionId,
            @Valid @RequestBody ChangeSubscriptionPlanRequest request
    ) {
        return ResponseEntity.ok(
                subscriptionService.changePlan(loginUser.id(), subscriptionId, request.planId())
        );
    }
}