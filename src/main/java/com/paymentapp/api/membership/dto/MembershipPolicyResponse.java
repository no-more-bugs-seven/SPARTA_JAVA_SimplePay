package com.paymentapp.api.membership.dto;

import com.paymentapp.api.membership.MembershipTier;

import java.math.BigDecimal;

public record MembershipPolicyResponse(
        Long tierId,
        String tierName,
        BigDecimal minSpentAmount,
        BigDecimal pointRate
) {
    public static MembershipPolicyResponse from(MembershipTier tier) {
        return new MembershipPolicyResponse(
                tier.getId(),
                tier.getName(),
                tier.getMinSpentAmount(),
                tier.getPointRate()
        );
    }
}
