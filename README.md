# ticklock

[![language](https://img.shields.io/badge/language-Java%20→%20Kotlin-blue)]()
[![framework](https://img.shields.io/badge/framework-Gradle-lightgrey)]()
[![topic](https://img.shields.io/badge/topic-Concurrency%20Control-orange)]()
[![domain](https://img.shields.io/badge/domain-Online%20Ticketing-informational)]()

온라인 티켓팅 도메인에서 다양한 동시성 제어 기법을 단계적으로 적용·비교·실험하는 프로젝트입니다.

---

## Roadmap

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

## Current Status

- Gradle + Java 21 기반 초기 프로젝트 셋업
- `Event` 도메인 클래스를 만들고, 순수 Java 환경에서 동시성 실험을 준비 중입니다.