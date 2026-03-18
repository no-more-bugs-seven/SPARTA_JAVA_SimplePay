package com.paymentapp.api.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscriptionCreateRequest {

    @NotNull(message = "요금제 ID는 필수입니다.")
    private Long planId;

    @NotBlank(message = "billingKey는 필수입니다.")
    private String billingKey;

    @NotBlank(message = "CustomerUid 필수입니다.")
    private String customerUid;
}
