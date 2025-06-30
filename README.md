# 🖼️ ImageSprint

고성능 이미지 변환 시스템  
멀티모듈 구조 기반의 Spring Boot 프로젝트입니다.

---

## 프로젝트 구조

``` bash
image-sprint/
├── core/               # 도메인 로직, 유즈케이스
├── api-server/         # 메인 API 서버 (Spring Boot)
├── worker-server/      # 이미지 변환 워커 (Spring Boot + Coroutine)
├── infrastructure/     # Redis, JPA, S3 등 외부 연동 구현
├── common/             # 공통 DTO, 유틸리티 등
├── build.gradle.kts    # 루트 빌드 설정
└── settings.gradle.kts # 루트 설정
```

---

## 기술 스택

- Kotlin 1.9.x
- Spring Boot 3.5.x
- MySQL / H2 (로컬 개발용)
- Redis (작업 큐)
- AWS S3 (이미지 저장)
- JPA + R2DBC (DB 접근)
- Gradle Kotlin DSL (멀티모듈)
- Coroutine (비동기 처리)
- Clean Architecture + DDD

---

## 모듈 설명

| 모듈            | 설명 |
|-----------------|------|
| `core`          | 도메인 모델 및 유즈케이스 |
| `api-server`    | 외부 API 제공, 인증, 알림 처리 |
| `worker-server` | 이미지 변환 전용 워커, Redis Queue 기반 |
| `infrastructure`| Redis, DB, S3 등 외부 연동 구현체 |
| `common`        | DTO, 유틸, 공통 설정 등 |

---

## 실행 방법

```bash
# 루트 디렉토리에서
./gradlew build
```

각 모듈은 독립적으로 실행 가능합니다.

---

## 멀티모듈 구성 이유

- 역할 분리: API 서버와 워커의 책임 분리
- 유지보수 용이: 의존성 관리와 테스트가 간편함
- 확장성 확보: 기능 추가 시 모듈 단위 확장 가능
