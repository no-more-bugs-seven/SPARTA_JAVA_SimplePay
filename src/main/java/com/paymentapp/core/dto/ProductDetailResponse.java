package com.paymentapp.core.dto;

/**
 * 상품 단건 DTO
 */

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductDetailResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String category;
    private String status;
}