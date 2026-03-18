package com.paymentapp.api.product;

import com.paymentapp.api.payment.entity.Payment;
import com.paymentapp.core.constant.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // PESSIMISTIC_WRITE: 쓰기 락을 걸어 다른 트랜잭션의 읽기/쓰기를 모두 차단
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :productId")
    Optional<Product> findByIdWithLock(Long productId);

    List<Product> findAllByStatus(ProductStatus status);
}