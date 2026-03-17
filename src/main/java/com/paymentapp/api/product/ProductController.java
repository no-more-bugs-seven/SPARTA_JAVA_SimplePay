package com.paymentapp.api.product;

import com.paymentapp.api.product.dto.ProductDetailResponse;
import com.paymentapp.api.product.dto.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // 상품 목록 조회
    @GetMapping
    public List<ProductListResponse> getProducts() {
        return productService.getProducts();
    }

    // 상품 단건 조회
    @GetMapping("/{productId}")
    public ProductDetailResponse getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }
}