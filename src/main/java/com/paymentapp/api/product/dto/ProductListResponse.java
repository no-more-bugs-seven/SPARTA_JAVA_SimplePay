package com.paymentapp.api.product.dto;

/**
* 상품 목록 DTO
 */

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductListResponse {

    private String id;
    private String name;
    private BigDecimal price;
    private Integer stock;

}
