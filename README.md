# ticklock

[![language](https://img.shields.io/badge/language-Kotlin-purple)]()
[![framework](https://img.shields.io/badge/framework-Spring%20Boot-green)]()
[![topic](https://img.shields.io/badge/topic-Concurrency%20Control-orange)]()
[![domain](https://img.shields.io/badge/domain-Online%20Ticketing-informational)]()

온라인 티켓팅 도메인에서 다양한 동시성 제어 기법을 **하나의 API에서 비교·실험**하는 프로젝트입니다.

---

## 🎯 핵심 기능

6가지 동시성 제어 방식을 **동일한 조건(DB 저장)**으로 비교할 수 있습니다.

| 락 방식 | 엔드포인트 | 단일 서버 | 분산 환경 |
|--------|-----------|:--------:|:--------:|
| No-Lock | `/api/events/{id}/purchase/no-lock` | ❌ 실패 | ❌ 실패 |
| synchronized | `/api/events/{id}/purchase/synchronized` | ✅ 성공 | ❌ 실패 |
| ReentrantLock | `/api/events/{id}/purchase/reentrant-lock` | ✅ 성공 | ❌ 실패 |
| DB 비관적 락 | `/api/events/{id}/purchase/pessimistic` | ✅ 성공 | ✅ 성공 |
| DB 낙관적 락 | `/api/events/{id}/purchase/optimistic` | ✅ 성공 | ✅ 성공 |
| Redis 분산 락 | `/api/events/{id}/purchase/redis` | ✅ 성공 | ✅ 성공 |

---

## 🚀 빠른 시작

### 1. 단일 서버 (H2 인메모리)

```bash
./gradlew bootRun
```

### 2. Docker 환경 (PostgreSQL + Redis)

```bash
docker-compose up -d
./gradlew bootRun --args='--spring.profiles.active=docker'
```

### 3. 분산 환경 (서버 3대 + Nginx)

```bash
docker-compose -f docker-compose-distributed.yml up -d
```

---

## 📡 API 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/events/{id}` | 이벤트 조회 |
| POST | `/api/events` | 이벤트 생성 |
| POST | `/api/events/{id}/purchase/no-lock` | 락 없음 (문제 발생) |
| POST | `/api/events/{id}/purchase/synchronized` | JVM 로컬 락 |
| POST | `/api/events/{id}/purchase/reentrant-lock` | JVM 로컬 락 |
| POST | `/api/events/{id}/purchase/pessimistic` | DB 비관적 락 |
| POST | `/api/events/{id}/purchase/optimistic` | DB 낙관적 락 |
| POST | `/api/events/{id}/purchase/redis` | Redis 분산 락 |

### 사용 예시

```bash
# 이벤트 생성 (100석)
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"name":"콘서트","totalSeats":100}'

# 티켓 구매 (synchronized)
curl -X POST http://localhost:8080/api/events/1/purchase/synchronized

# 이벤트 조회
curl http://localhost:8080/api/events/1
```

---

## 🔒 동시성 제어 방식 비교

### 1. No-Lock (문제 상황)

```kotlin
@Transactional
fun purchase(eventId: Long): Boolean {
    val event = eventRepository.findById(eventId).orElseThrow()
    if (!event.hasRemainingSeats()) return false
    event.decreaseSeat()  // Race Condition 발생!
    return true
}
```

- **문제**: 여러 스레드가 동시에 재고 체크 → 초과 판매
- **용도**: 문제 상황 재현용

### 2. synchronized (JVM 로컬 락)

```kotlin
synchronized(lock) {
    val event = eventRepository.findById(eventId).orElseThrow()
    if (!event.hasRemainingSeats()) return false
    event.decreaseSeat()
    return true
}
```

- **장점**: 구현 간단, 단일 서버에서 확실한 동시성 제어
- **단점**: 분산 환경에서 동작 안 함 (JVM 내부만 보호)

### 3. ReentrantLock (JVM 로컬 락)

```kotlin
val lock = locks.computeIfAbsent(eventId) { ReentrantLock() }
lock.lock()
try {
    // 비즈니스 로직
} finally {
    lock.unlock()
}
```

- **장점**: tryLock, timeout 등 세밀한 제어 가능
- **단점**: 분산 환경에서 동작 안 함

### 4. DB 비관적 락 (Pessimistic Lock)

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT e FROM EventEntity e WHERE e.id = :id")
fun findByIdWithPessimisticLock(id: Long): Optional<EventEntity>
```

- **동작**: `SELECT ... FOR UPDATE`로 행 잠금
- **장점**: 분산 환경에서도 동작 (DB가 락 관리)
- **단점**: 락 대기 시간 발생, 데드락 가능

### 5. DB 낙관적 락 (Optimistic Lock)

```kotlin
@Entity
class EventEntity(
    // ...
    @Version
    val version: Long? = null
)
```

- **동작**: 업데이트 시 버전 비교, 충돌 시 예외 발생
- **장점**: 락 대기 없음, 읽기 성능 좋음
- **단점**: 충돌 시 재시도 로직 필요

### 6. Redis 분산 락

```kotlin
val lock = redissonClient.getLock("ticket:event:$eventId")
if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
    try {
        // 비즈니스 로직
    } finally {
        lock.unlock()
    }
}
```

- **장점**: 분산 환경에서 빠른 락 획득, 확장성 좋음
- **단점**: Redis 의존성 추가, 구현 복잡

---

## 🏗️ 프로젝트 구조

```
src/main/kotlin/ticklock/
├── config/
│   └── RedisConfig.kt                    # Redisson 클라이언트 설정
├── controller/
│   ├── dto/
│   │   ├── EventResponse.kt
│   │   ├── PurchaseRequest.kt
│   │   └── PurchaseResponse.kt
│   ├── HelloController.kt                # 서버 상태 확인
│   └── UnifiedEventController.kt         # 통합 API (6가지 락 방식)
├── entity/
│   ├── EventEntity.kt                    # 이벤트 JPA 엔티티
│   └── TicketTypeEntity.kt               # 티켓 종류 JPA 엔티티
├── repository/
│   ├── EventRepository.kt                # 비관적/낙관적 락 쿼리
│   └── TicketTypeRepository.kt
├── service/
│   ├── unified/                          # 통합 서비스 (6가지 락 방식)
│   │   ├── UnifiedTicketPurchaseService.kt
│   │   ├── NoLockUnifiedService.kt
│   │   ├── SynchronizedUnifiedService.kt
│   │   ├── ReentrantLockUnifiedService.kt
│   │   ├── PessimisticLockUnifiedService.kt
│   │   ├── OptimisticLockUnifiedService.kt
│   │   ├── OptimisticLockExecutor.kt
│   │   └── RedisLockUnifiedService.kt
│   └── deadlock/                         # 데드락 실험
│       ├── DeadlockProneService.kt
│       └── DeadlockFreeService.kt
└── TicklockApplication.kt
```

---

## 🔬 분산 환경 아키텍처

```
                    ┌─────────────────┐
     요청 ────────▶ │     Nginx       │ (포트 80)
                    │  (로드밸런서)    │
                    └────────┬────────┘
            ┌────────────────┼────────────────┐
            ▼                ▼                ▼
       ┌─────────┐      ┌─────────┐      ┌─────────┐
       │  app1   │      │  app2   │      │  app3   │
       │ (Spring)│      │ (Spring)│      │ (Spring)│
       └────┬────┘      └────┬────┘      └────┬────┘
            │                │                │
            └────────────────┼────────────────┘
                             ▼
                    ┌─────────────────┐
                    │   PostgreSQL    │
                    └─────────────────┘
                             │
                    ┌─────────────────┐
                    │     Redis       │
                    └─────────────────┘
```

**왜 분산 락이 필요한가?**

```
1. synchronized로 단일 서버에서 해결됨
            ↓
2. 서버를 3대로 늘리면 synchronized 실패
            ↓
3. DB 락으로 해결되지만 성능 저하
            ↓
4. Redis 분산 락으로 빠르고 안전하게 해결
```

---

## 데드락 실험

### 문제 상황 (DeadlockProneService)

```
스레드 A: VIP 락 획득 → R석 락 획득 시도 (대기)
스레드 B: R석 락 획득 → VIP 락 획득 시도 (대기)
→ 서로 상대방의 락을 기다리며 무한 대기 (데드락)
```

### 해결 방법 (DeadlockFreeService)

```kotlin
// 항상 ID가 작은 것부터 락 획득
val firstId = min(ticketTypeId1, ticketTypeId2)
val secondId = max(ticketTypeId1, ticketTypeId2)
```

→ 락 순서가 통일되어 데드락 발생하지 않음

---

## 🛠️ 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 | Kotlin 1.9 |
| 프레임워크 | Spring Boot 3.2, Spring Data JPA |
| 데이터베이스 | H2 (개발), PostgreSQL (운영) |
| 분산 락 | Redis, Redisson |
| 인프라 | Docker, Docker Compose, Nginx |
| 테스트 | JUnit 5, Testcontainers |
| 빌드 | Gradle (Kotlin DSL) |

---

## 📚 학습 내용

1. **Race Condition 이해**: Check-Then-Act, Read-Modify-Write 패턴
2. **JVM 로컬 락**: synchronized vs ReentrantLock
3. **JPA 락**: 비관적 락 vs 낙관적 락
4. **데드락**: 발생 조건과 해결 방법 (락 순서 통일)
5. **분산 락**: 로컬 락의 한계와 Redis 분산 락
6. **Kotlin 마이그레이션**: Java → Kotlin 전환 경험

---

