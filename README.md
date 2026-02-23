# Loop Backend

Loop 서비스 백엔드 서버입니다.  
Spring Boot 기반 REST API, OAuth2/JWT 인증, WebSocket 채팅, AWS S3 파일 업로드를 제공합니다.

## 주요 기능

- 회원가입/로그인 및 JWT 인증
- Google OAuth2 로그인
- 게시글/댓글/좋아요
- 신고(Report) 기능
- 실시간 채팅(WebSocket + STOMP)
- 알림(Notification)
- 사용자/마이페이지
- 광고(Ad) 도메인

## 기술 스택

- Language: Java 21
- Framework: Spring Boot 3.5.4
- Security: Spring Security, JWT, OAuth2 Client
- Database: Spring Data JPA, Hibernate, MySQL
- Realtime: Spring WebSocket, STOMP, SockJS
- Storage: AWS S3 (`spring-cloud-aws-starter-s3`)
- Docs: springdoc-openapi (Swagger UI)
- Build: Gradle

## 메시징

- Kafka는 현재 사용하지 않습니다.
- 비동기/실시간 처리는 WebSocket 기반으로 구성되어 있습니다.

## 실행 방법

### 1) 환경 요구사항

- JDK 21
- MySQL
- Gradle Wrapper 사용 권장 (`./gradlew`)

### 2) 환경 변수 설정

`src/main/resources/application.yml` 기준으로 아래 값을 설정해야 합니다.

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `AWS_REGION`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_S3_BUCKET`
- `OAUTH2_REDIRECT_URI` (선택, 미설정 시 기본값 사용)

### 3) 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 포트: `8080`

## API 문서

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 프로젝트 구조

```text
src/main/java/server/loop
├── LoopApplication.java
├── domain
│   ├── ad
│   ├── auth
│   ├── chat
│   ├── notification
│   ├── post
│   ├── report
│   └── user
└── global
    ├── common
    ├── config
    └── security
```
