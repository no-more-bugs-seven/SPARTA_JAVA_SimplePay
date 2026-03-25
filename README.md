<h1 align="center">💳 7전8기의 암시장</h1>

<p align="center">
  결제 및 구독 서비스<br>
</p>

---

<p align="center">

<img src="https://img.shields.io/badge/Java-17-red">
<img src="https://img.shields.io/badge/SpringBoot-4.x-green">
<img src="https://img.shields.io/badge/JPA-Hibernate-orange">
<img src="https://img.shields.io/badge/MySQL-8-blue">
<img src="https://img.shields.io/badge/Gradle-8-02303A">
<img src="https://img.shields.io/badge/GitHub-Repository-black">

</p>

---

## 📌 프로젝트 소개

이 프로젝트는 PortOne(KG이니시스) 연동을 통한 일반·정기 결제와 사용자 맞춤형 멤버십 혜택을 제공하는 고도화된 이커머스 서비스입니다.
사용자는 장바구니 주문부터 포인트 차감 결제까지 매끄러운 구매 여정을 경험하며, 
서버는 결제 검증 및 멱등성 설계를 통해 데이터의 무결성과 신뢰성 있는 환불 프로세스를 보장합니다.
또한, 누적 결제액 기반의 자동 등급 갱신 시스템을 통해 차등화된 포인트 적립률을 적용함으로써 고객 충성도를 높이는 비즈니스 로직을 구현했습니다.

요구사항
- 사용자는 회원가입 및 로그인을 통해 인증된 상태에서 서비스를 이용할 수 있어야 한다.
- 사용자는 결제가 가능한 상품 목록 및 상품 상세 정보를 조회할 수 있어야 한다.
- 사용자는 여러 상품을 하나의 주문으로 생성하고 주문 내역 및 상세 정보를 조회할 수 있어야 한다.
- PortOne + KG이니시스 PG를 통해 카드 결제를 진행할 수 있어야 한다.
- 사용자는 포인트를 사용하여 결제 금액을 일부 또는 전부 차감할 수 있어야 한다.
- 사용자의 총 결제 금액에 따라 멤버십 등급이 자동 갱신되며 등급별 포인트 적립률이 적용된다.
- 결제 완료된 주문에 대해 전액 환불이 가능하며 사용 포인트 복구 및 적립 포인트 취소가 이루어져야 한다.
- 서버는 PortOne 결제 조회 API를 통해 결제를 검증하며 중복 요청에도 동일한 결과를 보장해야 한다.
- 사용자는 결제 수단을 등록하고 빌링키 기반 정기 결제를 이용할 수 있어야 한다.

---

## 👥 팀소개

| 이름  | 역할 | 담당                        |
|-----|----|---------------------------|
| 김소현 | 팀장 | 일반 결제 및 웹훅 API 개발 |
| 성기찬 | 팀원 | 인증 인가 API 개발 및 CI/CD 구축, AWS 인프라 구축 |
| 고은지 | 팀원 | 포인트 결제 API 개발 |
| 박영수 | 팀원 | 구독 결제 API 개발  |
| 이지민 | 팀원 | 상품, 주문 API 개발 |

<br>

### [📎프로젝트 노션 바로가기](https://www.notion.so/teamsparta/7-8-31e2dc3ef51480398ef6eff4980e2f2e)

<br>

---

## ⏲️ 개발기간
- 2026.03.16(월) ~ 2026.03.27(금)

---

## 🧩 Architecture

<p align="center">
  <img src="docs/images/Architecture_Diagram.png" width="80%">
</p>

---

## 🔧 Technologies & Tools (BE)

<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/> <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white"/>

<img src="https://img.shields.io/badge/SpringSecurity-6DB33F?style=for-the-badge&logo=SpringSecurity&logoColor=white"/> <img src="https://img.shields.io/badge/JSONWebToken-000000?style=for-the-badge&logo=JSONWebTokens&logoColor=white"/> 

<img src="https://img.shields.io/badge/SpringDataJPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/> <img src="https://img.shields.io/badge/QueryDSL-0769AD?style=for-the-badge&logo=java&logoColor=white"/> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white"/>

<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white"/> <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"/>

<img src="https://img.shields.io/badge/IntelliJIDEA-000000?style=for-the-badge&logo=IntelliJIDEA&logoColor=white"/> <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=Postman&logoColor=white"/> <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=Notion&logoColor=white"/> <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white"/>

---

## 🧠 적용 기술
#### ◻ Spring Security
> 인가/인증 처리를 필터 체인 기반으로 구성하여 API 접근 권한을 통제하고, 인증되지 않은 요청을 차단하는 보안 구조를 적용했습니다.

#### ◻ JWT
> 세션 대신 JWT 기반 인증 방식을 사용하여 구현했습니다.

#### ◻ Soft Delete
> 데이터를 실제 삭제하지 않고 deleted 플래그를 활용해 논리 삭제 처리함으로써 데이터 복구 가능성과 이력 추적성을 확보했습니다.

#### ◻ PasswordEncoder
> 비밀번호를 단방향 해시 방식으로 암호화하여 DB에 평문이 저장되지 않도록 하고, 로그인 시 안전한 비교가 가능하도록 구현했습니다.

#### ◻ 전역 예외 처리 (CommonError, CommonException, GlobalExceptionHandler)
> 비즈니스 예외와 시스템 예외를 분리하고, 전역 예외 처리기를 통해 일관된 에러 응답 형식을 제공하여 클라이언트가 오류 상황을 명확히 인지할 수 있도록 했습니다.

#### ◻ JPA / JPQL
> 객체 중심의 데이터 접근을 위해 JPA를 사용하고, 복잡한 조회는 JPQL을 활용하여 엔티티 기반 쿼리를 작성함으로써 유지보수성과 가독성을 높였습니다.

#### ◻ QueryDSL
> 정렬/검색 조건이 동적으로 변하는 조회 API에 QueryDSL을 적용하여 타입 안정성을 확보하고, 복잡한 조건 조합을 코드 기반으로 안전하게 구성했습니다.

#### ◻ Builder + record
> DTO 생성 시 Builder 패턴을 사용해 가독성과 유지보수성을 높이고, 불변 데이터 전달 객체에는 record를 활용하여 코드량을 줄이고 안정성을 확보했습니다.

#### ◻ Validation
> 요청 데이터에 대해 길이 제한, 필수값 검증 등 입력 검증 로직을 적용하여 잘못된 요청을 사전에 차단하고 서비스 안정성을 높였습니다.

#### ◻ 페이징 조회
> 대량 데이터 조회 시 Page 기반 페이징 처리를 적용하여 응답 속도를 개선하고, 클라이언트가 필요한 데이터만 효율적으로 조회할 수 있도록 구현했습니다.

#### ◻ BaseEntity
> 엔티티 공통 필드(createdAt, modifiedAt 등)를 BaseEntity로 분리하여 중복 코드를 제거하고, 모든 도메인에서 동일한 감사(Auditing) 정책을 적용할 수 있도록 설계했습니다.

#### ◻ 공통 응답 DTO (ApiResponse)
> API 응답 구조를 ApiResponse<T> 형태로 통일하여 성공/실패 응답 형식을 일관되게 유지하고, 프론트엔드가 상태·메시지·데이터를 예측 가능하게 처리할 수 있도록 했습니다.

#### ◻ Postman
> API 테스트 도구로 Postman을 활용하여 엔드포인트 검증, 인증 흐름 테스트, 요청/응답 구조 확인 등을 수행했습니다.

---

## 🚀 주요 기능

### Admin
- 관리자 회원가입/로그인/로그아웃
- 관리자 목록/상세 조회
- 관리자 정보 수정
- 관리자 역할/상태 변경
- 관리자 삭제 (Soft Delete)
- 관리자 승인/거부 처리
- 내 프로필 조회/수정
- 비밀번호 변경

### Customer
- 고객 목록/상세 조회
- 고객 정보 수정
- 고객 상태 변경
- 고객 삭제 (Soft Delete)

### Dashboard
- 관리자 / 고객 / 상품 / 주문 / 리뷰 카운트
- 총 매출 / 상태별 주문 수 집계
- 리뷰 평점 분포 차트
- 고객 상태 분포
- 카테고리 분포
- 최근 주문 목록 조회

### Order
- 주문 생성
- 주문 목록/상세 조회
- 주문 상태 변경
- 주문 취소

### Product
- 상품 등록
- 상품 목록/상세 조회
- 상품 정보 수정
- 상품 재고/상태 변경
- 상품 삭제 (Soft Delete)

### Review
- 리뷰 목록/상세 조회
- 리뷰 삭제
- 상품별 리뷰 조회

---

## 🖥 Development Environment

| 항목 | 버전  |
|---|-----|
| Java | 17  |
| Spring Boot | 4.x |
| Gradle | 8.x |
| MySQL | 8.x |
| JPA | Hibernate |
| IDE | IntelliJ |

---

## 🖼 API 명세서

<p align="center">
  <img src="docs/images/API_명세서.png" width="80%">
</p>

보다 자세한 API 명세서는
[📎프로젝트 노션](https://www.notion.so/teamsparta/7-8-31e2dc3ef51480398ef6eff4980e2f2e) 에서 확인할 수 있습니다.

---

## 🗄 ERD Diagram

<p align="center">
  <img src="docs/images/erd.png" width="100%">
</p>

---

## 🧪 TestCase

<p align="center">
  <img src="docs/images/" width="100%">
</p>

---

## 📈 프로젝트 파일 구조

```text
src/main/java/com/commerce/manageit/
├── domain/                    # 핵심 비즈니스 로직 (도메인별 분리)
│   ├── admin/                 # 관리자(Admin) 관련 도메인
│   │   ├── controller/        # API 엔드포인트
│   │   ├── dto/               # Request / Response 객체
│   │   ├── entity/            # JPA 엔티티 (Domain Model)
│   │   ├── enums/             # 상태 코드 및 role enum
│   │   ├── repository/        # DB 접근 계층
│   │   └── service/           # 비즈니스 로직
│   │
│   ├── customer/              # 고객(Customer) 관련 도메인
│   ├── dashboard/             # 대시보드(Dashboard) 관련 도메인
│   ├── order/                 # 주문(Order) 관련 도메인
│   ├── product/               # 상품(Product) 관련 도메인
│   └── review/                # 리뷰(Review) 관련 도메인
│   
├── global/                    # 프로젝트 전역 공통 설정
│   ├── common/                # 공통 추상 클래스 (BaseEntity, ApiResponse)
│   ├── error/                 # 예외 처리 (ExceptionHandler, ErrorCode)
│   └── security/              # Framework 설정 (Security, JWT 등)
└── ECommerceBackofficeApplication.java   # 프로젝트 메인 실행 클래스
```

---

## 🚨 Trouble Shooting

👉 [결제 시스템의 재고 정합성 문제와 비관적 락(Pessimistic Lock) 적용](docs/troubleshooting/Pessimistic-Lock-for-Inventory-Consistency.md) <br>

---
