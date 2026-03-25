package com.paymentapp.api.product;

import com.paymentapp.api.product.dto.ProductDetailResponse;
import com.paymentapp.api.product.dto.ProductListResponse;
import com.paymentapp.api.product.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 목록 조회
    public List<ProductListResponse> getProducts() {
        return productRepository.findAllByStatus(ProductStatus.ON_SALE)
                .stream()
                .map(product -> ProductListResponse.builder()
                        .id(product.getId().toString())
                        .name(product.getName())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .build()
                )
                .toList();
    }

    // 상품 단건 조회
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .category(product.getCategory())
                .status(product.getStatus().name())
                .build();
    }
}