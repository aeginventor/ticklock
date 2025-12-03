# ticklock

[![language](https://img.shields.io/badge/language-Java%20→%20Kotlin-blue)]()
[![framework](https://img.shields.io/badge/framework-Gradle-lightgrey)]()
[![topic](https://img.shields.io/badge/topic-Concurrency%20Control-orange)]()
[![domain](https://img.shields.io/badge/domain-Online%20Ticketing-informational)]()

온라인 티켓팅 도메인에서 다양한 동시성 제어 기법을 단계적으로 적용·비교·실험하는 프로젝트입니다.

---

## 로드맵

이 프로젝트는 다음과 같은 단계로 발전할 예정입니다.

1. **Step 1 – Pure Java**
    - 온라인 티켓팅 도메인 간단히 정의
    - 여러 스레드가 동시에 예매를 시도하는 상황을 순수 Java로 시뮬레이션
    - 노락(No Lock), 로컬 락(synchronized, ReentrantLock)부터 실험

2. **Step 2 – Java + Spring Boot**
    - 웹 API 형태의 온라인 티켓팅 서비스로 확장
    - JPA 기반 비관적 락 / 낙관적 락(@Version + 재시도)
    - Redis + Redisson 분산 락
    - 데이터베이스 락 타임아웃, 데드락 재현 및 처리, 락 대기 시간 모니터링

3. **Step 3 – Kotlin + Spring Boot**
    - 2단계에서 만든 서비스를 Kotlin으로 마이그레이션
    - 기존 기능 유지 + Kotlin 문법과 스타일 적용

---

## 진행도

### ✅ Step 1 – Pure Java (완료)

순수 Java 환경에서 4가지 동시성 제어 방식을 구현하고 비교 실험을 완료했습니다.

#### 구현된 동시성 제어 방식

1. **No-Lock (문제 상황 재현)**
   - 동시성 제어를 하지 않은 상태
   - 2가지 Race Condition 발생
     - 경쟁 조건 #1: 재고 체크 단계 (Check-Then-Act 패턴)
     - 경쟁 조건 #2: 재고 감소 단계 (Read-Modify-Write 패턴)
   - 초과 판매, 음수 재고, 데이터 불일치 발생

2. **synchronized**
   - Java 키워드 수준의 동기화
   - 블록을 벗어나면 자동 unlock
   - 가장 간단하고 직관적
   - 실무에서 가장 많이 사용되는 방식
   - JVM 수준 최적화 지원

3. **ReentrantLock**
   - `java.util.concurrent.locks.ReentrantLock` 사용
   - 명시적으로 lock()/unlock() 호출 필요
   - tryLock(), timeout, 공정성(fairness) 등 고급 기능 제공
   - synchronized로 해결 안 되는 복잡한 시나리오에 사용
   - finally 블록에서 unlock 필수

4. **AtomicInteger**
   - `java.util.concurrent.atomic.AtomicInteger` 사용
   - CAS(Compare-And-Swap) 연산으로 원자성 보장
   - 락 없이 동시성 제어 (Lock-Free 알고리즘)
   - 데드락 위험 없음, 높은 성능
   - 단순한 숫자 연산에 적합

#### 프로젝트 구조

```
src/main/java/ticklock/
├── domain/
│   └── Event.java                              # 이벤트 도메인 모델
├── service/
│   ├── TicketPurchaseService.java              # 공통 인터페이스
│   ├── NoLockTicketPurchaseService.java        # 문제 상황
│   ├── SynchronizedTicketPurchaseService.java  # 동시성 제어 전략 1
│   ├── ReentrantLockTicketPurchaseService.java # 동시성 제어 전략 2
│   └── AtomicTicketPurchaseService.java        # 동시성 제어 전략 3
├── simulation/
│   ├── NoLockSimulation.java
│   ├── SynchronizedSimulation.java
│   ├── ReentrantLockSimulation.java
│   └── AtomicSimulation.java
└── Main.java                                   # 모든 시뮬레이션 실행

src/test/java/ticklock/
└── service/
    ├── NoLockTicketPurchaseServiceTest.java
    ├── SynchronizedTicketPurchaseServiceTest.java
    ├── ReentrantLockTicketPurchaseServiceTest.java
    └── AtomicTicketPurchaseServiceTest.java
```

#### 실행 방법

```bash
# 모든 시뮬레이션 실행
./gradlew run

# 테스트 실행
./gradlew test
```

#### 실험 결과 요약

| 방식 | 초과 판매 | 데이터 일관성 | 성능 | 복잡도 | 사용 시점 |
|------|----------|-------------|------|-------|----------|
| No-Lock | ❌ 발생 | ❌ 불일치 | ⚡ 매우 빠름 | ✅ 간단 | 동시성이 필요없을 때 |
| synchronized | ✅ 방지 | ✅ 보장 | 🐢 보통 | ✅ 간단 | 대부분의 경우 (기본 선택) |
| ReentrantLock | ✅ 방지 | ✅ 보장 | 🐢 보통 | ⚠️ 복잡 | timeout, tryLock 필요시 |
| AtomicInteger | ✅ 방지 | ✅ 보장 | ⚡ 빠름 | ⚠️ 복잡 | 단순 숫자 연산 + 고성능 |

#### 핵심 학습 내용

**동시성 문제 이해**
- **Race Condition**: 여러 스레드가 공유 자원에 동시 접근할 때 발생하는 문제
- **원자성(Atomicity)**: 연산이 중간에 끼어들 수 없이 완전히 실행되어야 함
- **Check-Then-Act**: 체크와 실행 사이에 다른 스레드가 끼어들 수 있음
- **Read-Modify-Write**: 읽기-수정-쓰기가 원자적이지 않으면 데이터 손실 발생

**동시성 제어 전략**
- **synchronized**: 가장 기본적이고 실용적인 해결책
- **ReentrantLock**: synchronized의 한계를 극복하는 고급 락
- **Lock-Free (CAS)**: 락 없이 원자적 연산으로 동시성 제어

**설계 원칙**
- 간단한 경우 synchronized로 시작
- 복잡한 요구사항이 있을 때만 ReentrantLock 고려
- 단순 숫자 연산은 Atomic 클래스 사용
- 성능보다 정확성이 우선

---

### Step 2 – Java + Spring Boot (계획)

웹 API 형태의 온라인 티켓팅 서비스로 확장하여, 실무 환경에서의 동시성 제어를 실험합니다.

#### Step 2-1: 단일 서버 + DB 락

**목표**: 데이터베이스 레벨 동시성 제어 이해

| 구현 | 설명 |
|------|------|
| No-Lock | 동시성 제어 없이 문제 상황 재현 |
| 비관적 락 | `@Lock(PESSIMISTIC_WRITE)` - SELECT FOR UPDATE |
| 낙관적 락 | `@Version` + 재시도 로직 |

**기술 스택**: Spring Boot, JPA, H2/PostgreSQL

#### Step 2-2: 복잡한 도메인

**목표**: 실무에서 발생하는 동시성 문제 경험

**도메인 확장**:
```
Event (공연)
├── TicketType (티켓 종류)
│   ├── VIP석 (50석, 150,000원)
│   ├── R석 (100석, 100,000원)
│   └── S석 (200석, 70,000원)
└── Seat (좌석) - 선택 예매용
```

**시나리오**:
- 동일 좌석 동시 예매
- 데드락 재현 및 해결
- 락 타임아웃 처리
- 트랜잭션 격리 수준 비교

#### Step 2-3: 분산 환경

**목표**: 여러 서버에서 동시 요청 시 동시성 제어

**아키텍처**:
```
                    ┌─────────────────┐
                    │  Load Balancer  │
                    │     (Nginx)     │
                    └────────┬────────┘
            ┌────────────────┼────────────────┐
            ▼                ▼                ▼
     ┌────────────┐   ┌────────────┐   ┌────────────┐
     │  Server 1  │   │  Server 2  │   │  Server 3  │
     │  (Spring)  │   │  (Spring)  │   │  (Spring)  │
     └─────┬──────┘   └─────┬──────┘   └─────┬──────┘
           │                │                │
           └────────────────┼────────────────┘
                            ▼
                ┌───────────────────────┐
                │      PostgreSQL       │
                └───────────────────────┘
                            │
                ┌───────────────────────┐
                │   Redis (분산 락)      │
                └───────────────────────┘
```

**비교 실험**:

| 방식 | 단일 서버 | 분산 환경 (3대) | 비고 |
|------|:--------:|:--------------:|------|
| synchronized | ✅ | ❌ | JVM 내부에서만 동작 |
| DB 비관적 락 | ✅ | ✅ | DB가 보장, 느림 |
| DB 낙관적 락 | ✅ | ✅ | 충돌 시 재시도 필요 |
| Redis 분산 락 | ✅ | ✅ | 빠름, 권장 |

**기술 스택**: Docker Compose, Redis, Redisson, k6 (부하 테스트)

#### Step 2 핵심 학습 목표

**"왜 분산 락이 필요한가?"를 단계적으로 증명**:

```
1. synchronized로 단일 서버에서 해결됨
              ↓
2. 서버를 3대로 늘리면 synchronized 실패
              ↓
3. DB 락으로 해결되지만 성능 저하
              ↓
4. Redis 분산 락으로 빠르고 안전하게 해결
```

**학습 내용**:
- JPA 락 메커니즘 (비관적/낙관적)
- 데드락 발생 조건과 해결 방법
- 분산 환경에서 로컬 락의 한계
- Redis 분산 락 (Redisson) 동작 원리
- 부하 테스트 및 성능 측정