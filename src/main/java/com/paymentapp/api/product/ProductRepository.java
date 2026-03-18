package com.paymentapp.api.product;

import com.paymentapp.core.constant.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByStatus(ProductStatus status);
}