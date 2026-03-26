## 🚩 [Troubleshooting] 결제 시스템의 재고 정합성 문제와 비관적 락(Pessimistic Lock) 적용

## 1. 배경 및 문제 상황
결제 시스템에서 같은 상품에 대한 주문을 여러 명의 사용자가 동시에 '결제하기' 버튼을 누르는 상황을 가정했습니다. 
이때 여러 트랜잭션이 동일한 상품의 재고 데이터(`Stock`)에 접근하여 수정하면서 다음과 같은 심각한 문제가 발생했습니다.

* **현상**: 재고가 10개만 남았음에도 불구하고, 결제 완료된 주문이 15개 이상 발생하는 **초과 판매(Over-selling)** 현상.
* **원인**: **Race Condition(경쟁 상태)**. 두 개 이상의 트랜잭션이 공유 자원(상품 재고)에 동시에 접근하여 데이터를 읽고 수정할 때, 먼저 수정된 데이터가 유실되거나(Lost Update) 잘못된 기초 데이터를 바탕으로 연산이 수행되었습니다.

---

## 2. 해결 방안 고민: 낙관적 락 vs 비관적 락

동시성 제어를 위해 두 가지 전략을 비교 분석하였습니다.

### A. 낙관적 락 (Optimistic Lock)
* **개념**: 데이터 충돌이 자주 발생하지 않을 것이라고 '낙관적'으로 가정하는 방식입니다.
* **원리**: JPA의 `@Version`을 활용하여 수정 시점에 내가 읽은 버전이 맞는지 확인합니다. (`UPDATE ... WHERE version = ?`)
* **장점**: DB 수준의 락을 걸지 않아 성능상 이점이 있습니다.
* **단점**: 충돌 발생 시 애플리케이션에서 **재시도(Retry) 로직**을 직접 구현해야 하며, 결제와 같이 충돌이 빈번한 환경에서는 재시도 오버헤드로 인해 오히려 성능이 저하됩니다.

### B. 비관적 락 (Pessimistic Lock) - **최종 선택**
* **개념**: 데이터 충돌이 반드시 발생할 것이라고 '비관적'으로 가정하고 미리 락을 거는 방식입니다.
* **원리**: DB 수준에서 `SELECT ... FOR UPDATE` 구문을 사용하여 해당 로우(Row)를 점유합니다.
* **장점**: **데이터 정합성을 확실히 보장**합니다. 충돌 시 다른 트랜잭션은 대기 상태로 들어가므로 별도의 재시도 로직이 필요 없습니다.
* **단점**: 락 점유 기간 동안 다른 트랜잭션이 대기하므로 성능 저하 우려가 있고, 설계 미숙 시 **데드락(Deadlock)** 위험이 있습니다.

---

## 3. 상세 구현 전략

`PaymentService`의 `changeStock` 메서드에서 성능과 안정성을 극대화하기 위해 다음 전략을 사용했습니다.

### ① 일괄 조회 및 비관적 락 획득 (Bulk Lock)
루프를 돌며 상품을 하나씩 조회하는 대신, 주문에 포함된 모든 상품 ID를 수집하여 한 번의 쿼리로 락을 획득합니다. 
이는 네트워크 왕복 시간을 줄이고 락 점유 시간을 최소화합니다.

```java
private void changeStock(Order order, String type) {
    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

    // 1. 상품 ID 리스트 추출 (데드락 방지를 위해 정렬)
    List<Long> productIds = items.stream()
            .map(item -> item.getProduct().getId())
            .sorted() // 매우 중요: ID 순 정렬을 통한 순환 대기(Deadlock) 방지
            .toList();

    // 2. 비관적 락 획득 (SELECT ... FOR UPDATE)
    // 리포지토리의 findAllByIdsWithLock은 @Lock(LockModeType.PESSIMISTIC_WRITE)를 사용함
    List<Product> products = productRepository.findAllByIdsWithLock(productIds);

    // 3. 재고 차감/복구 연산 수행
    for (OrderItem item : items) {
        Product product = products.stream()
                .filter(p -> p.getId().equals(item.getProduct().getId()))
                .findFirst()
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if ("restore".equals(type)) {
            product.increaseStock(item.getQuantity());
        } else {
            product.decreaseStock(item.getQuantity());
        }
    }
}
```

### ② 보상 트랜잭션(Compensation)과의 연계
결제 프로세스 도중(금액 불일치, 재고 부족, 시스템 오류 등) 예외가 발생하면 `handleCompensation` 메서드를 통해 점유했던 재고를 즉시 복구(`restore`)하도록 설계하여 데이터의 최종 일관성을 유지했습니다.

특히, 외부 PG사(PortOne)의 결제는 성공했으나 내부 DB 작업(포인트 적립, 등급 갱신 등) 중 오류가 발생할 경우를 대비하여, API 취소 호출과 재고 복구가 원자적으로 실행되도록 구성했습니다.

---

## 4. 결론 및 요약

비관적 락 도입을 통해 트래픽이 몰리는 상황에서도 단 하나의 오차 없는 재고 관리가 가능해졌습니다.

| 구분 | 낙관적 락 (Optimistic) | 비관적 락 (Pessimistic) |
| :--- | :--- | :--- |
| **적용 시점** | 충돌이 적은 일반 기능 | **충돌이 잦은 핵심 비즈니스 (결제)** |
| **관리 주체** | 애플리케이션 (JPA) | 데이터베이스 (RDBMS) |
| **실패 처리** | 예외 던지기 -> 재시도 필요 | 대기(Queueing) -> 순차 처리 |
| **정합성** | 낮음 (충돌 시 유실 가능성) | **매우 높음** |

---
> **💡 Insight**: 비관적 락 사용 시 발생할 수 있는 데드락은 **자원 접근 순서(ID 정렬)**를 고정함으로써 예방할 수 있었습니다. 
> 또한, 재고 변경 로직을 서비스 레이어 내 별도 메서드로 분리하여 락의 점유 범위를 명확히 관리함으로써 트랜잭션 효율을 높였습니다.