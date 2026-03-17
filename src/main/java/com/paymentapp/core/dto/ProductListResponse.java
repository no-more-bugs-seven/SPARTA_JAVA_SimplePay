package com.paymentapp.core.dto;

/**
* 상품 목록 DTO
 */

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductListResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;

}
