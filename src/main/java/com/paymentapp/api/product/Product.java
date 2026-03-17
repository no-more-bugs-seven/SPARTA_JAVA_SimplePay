package com.paymentapp.api.product;

import com.paymentapp.core.constant.ProductStatus;
import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품명
    @Column(nullable = false, length = 100)
    private String name;

    // 가격
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // 재고
    @Column(nullable = false)
    private Integer stock;

    // 설명
    @Column(length = 500)
    private String description;

    // 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status;

    // 카테고리
    @Column(nullable = false, length = 50)
    private String category;

    public Product(String name, BigDecimal price, Integer stock, String description,
                   ProductStatus status, String category) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.status = status;
        this.category = category;
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }
}