<div align="center">
  <br />
  <h1 style="border-bottom: none; font-size: 3.5em; background: linear-gradient(to right, #b19cd9, #3b0a45, #b19cd9); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">
    7전8기의 암시장
  </h1>
  <p style="color: #8b949e; font-size: 1.2em; letter-spacing: 2px;">
    <b>THE RELENTLESS BLACK MARKET</b>
  </p>
  <hr style="background: linear-gradient(to right, transparent, #30363d, transparent); height: 1px; border: none;" />
  <br />
</div>

<p align="center">
  <img src="https://img.shields.io/badge/Java%2017-007396?style=flat-square&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Boot%203.x-6DB33F?style=flat-square&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=flat-square&logo=spring&logoColor=white">
  <br>
  <img src="https://img.shields.io/badge/MySQL%208.0-4479A1?style=flat-square&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white">
  <img src="https://img.shields.io/badge/PortOne-Payment-orange?style=flat-square">
  <br>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white">
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=flat-square&logo=junit5&logoColor=white">
  <img src="https://img.shields.io/badge/GitHub-Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white">
</p>

---

## 📌 프로젝트 소개

**7전8기 암시장**은 PortOne(KG이니시스)을 활용한 **일반·정기 결제 시스템**과 고객 충성도를 극대화하는 **멤버십 기반 이커머스 솔루션**입니다.  
단순한 결제 처리를 넘어, **결제 검증 및 보상 트랜잭션**을 통해 금융 수준의 데이터 무결성을 지향합니다.

---

### 💳 결제 및 구독 (Payment & Subscription)
* **결제 통합:** PortOne + KG이니시스 PG 연동을 통한 신용카드 일반 결제 구현
* **정기 결제:** 빌링키 기반의 자동 결제 수단 등록 및 구독 서비스 제공
* **결제 검증:** 웹훅(Webhook) 및 PortOne API를 통한 결제 금액 위변조 방지 및 멱등성 보장

### 💎 멤버십 및 포인트 (Loyalty Program)
* **자동 등급 관리:** 누적 결제액에 따른 실시간 멤버십 등급 갱신 시스템
* **스마트 포인트:** 등급별 차등 적립률 적용 및 결제 시 포인트 복합 사용 지원
* **신뢰 기반 환불:** 주문 취소 시 실시간 결제 취소, 포인트 회수 및 복구의 원자성 보장

### 🛡️ 시스템 안정성 (Reliability)
* **재고 정합성:** 비관적 락(`Pessimistic Lock`)을 적용하여 고가용성 환경에서도 정확한 재고 관리
* **보상 트랜잭션:** 외부 API 장애 및 재고 부족 시 자동 취소 및 데이터 롤백 처리
* **인증 및 인가:** Spring Security 기반의 안전한 회원가입 및 주문 프로세스

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

## 🔧 Technologies & Tools

### 🖥️ Backend Stack
<p align="left">
  <img src="https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Boot%203.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
  <img src="https://img.shields.io/badge/JSON%20Web%20Tokens-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">
</p>

### 💾 Data & Infrastructure
<p align="left">
  <img src="https://img.shields.io/badge/MySQL%208.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white">
</p>

### 🧪 Quality & DevOps
<p align="left">
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white">
  <img src="https://img.shields.io/badge/Mockito-000000?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
</p>

### 🤝 Collaboration & Management
<p align="left">
  <img src="https://img.shields.io/badge/Jira-0052CC?style=for-the-badge&logo=jira&logoColor=white">
  <img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white">
  <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white">
  <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white">
</p>

---

## 🧠 적용 기술 (Technical Decisions)

#### ◻ **비관적 락 (Pessimistic Lock)**
> 재고 차감 시 발생할 수 있는 동시성 문제를 해결하기 위해 DB 수준의 배타적 잠금(`PESSIMISTIC_WRITE`)을 적용하여 데이터 정합성을 보장했습니다.

#### ◻ **보상 트랜잭션 (Compensatory Transaction)**
> 결제 과정 중 재고 부족이나 외부 API 장애 발생 시, 이미 처리된 외부 결제 취소 및 포인트 복구를 수행하여 서비스의 원자성(Atomicity)을 확보했습니다.

#### ◻ **Redis**
> 정기 결제 빌링키 관리나 빈번한 조회 데이터의 캐싱을 고려하여 In-memory 데이터 구조를 활용하고, 시스템의 응답 속도와 효율성을 높였습니다.

#### ◻ **JUnit5 & Mockito**
> 단위 테스트와 통합 테스트를 통해 비즈니스 로직의 신뢰성을 검증하고, 특히 다중 스레드 환경에서의 동시성 제어 로직을 코드로 증명했습니다.

#### ◻ **Spring Security & JWT**
> 인증 및 인가 처리를 필터 체인 기반으로 구성하여 API 접근 권한을 통제하고, Stateless한 JWT 인증 방식을 통해 서버 확장이 용이한 보안 구조를 설계했습니다.

#### ◻ **AWS (EC2, RDS)**
> 클라우드 인프라를 활용하여 안정적인 서버 운영 환경을 구축하고, 데이터베이스와 정적 리소스를 분리하여 관리 효율성을 극대화했습니다.

#### ◻ **Jira & Agile Methodology**
> Jira를 통해 백로그 관리 및 스프린트 단위의 태스크 배분을 수행하며, 프로젝트 진행 상황을 투명하게 관리하고 협업 효율을 높였습니다.

#### ◻ **Builder + Record**
> 객체 생성 시 Builder 패턴으로 가독성을 높이고, 불변 데이터 전달 객체에는 자바 17의 `record`를 활용하여 보일러플레이트 코드를 줄이고 안정성을 강화했습니다.

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
