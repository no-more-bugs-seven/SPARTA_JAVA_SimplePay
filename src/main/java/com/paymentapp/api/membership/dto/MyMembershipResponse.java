package com.paymentapp.api.membership.dto;

import com.paymentapp.api.membership.MembershipTier;

import java.math.BigDecimal;

public record MyMembershipResponse(
        Long tierId,
        String tierName,
        BigDecimal pointRate
) {
    public static MyMembershipResponse from(MembershipTier tier) {
        return new MyMembershipResponse(
                tier.getId(),
                tier.getName(),
                tier.getPointRate()
        );
    }
}
