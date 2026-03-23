package com.paymentapp.api.subscription.dto;

import java.util.List;

public record BillingHistoryListResponse(
        List<BillingHistoryResponse> billings
) {}
