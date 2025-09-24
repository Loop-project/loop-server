# Loop Backend

이 프로젝트는 Loop 애플리케이션의 백엔드 서버입니다. Spring Boot를 기반으로 구축되었으며, 소셜 네트워킹 기능과 실시간 채팅을 제공합니다.

## 주요 기능

- **사용자 인증**: JWT를 사용한 회원가입 및 로그인
- **게시글**: 게시글 생성, 조회, 수정, 삭제
- **댓글**: 게시글에 대한 댓글 작성 및 관리
- **좋아요**: 게시글 좋아요 기능
- **신고**: 부적절한 게시글 신고
- **실시간 채팅**: WebSocket을 이용한 공개 및 비공개 채팅방
- **알림**: 새로운 활동에 대한 실시간 알림
- **마이페이지**: 사용자 프로필 정보 및 활동 내역 관리

## 기술 스택

- **Framework**: Spring Boot 3
- **Security**: Spring Security, JWT
- **Database**: Spring Data JPA, Hibernate, MySQL
- **Real-time**: WebSocket (STOMP)
- **Build**: Gradle
- **API Documentation**: Swagger UI
- **File Storage**: AWS S3

## 디렉토리 구조

```
src
└── main
    ├── java
    │   └── server
    │       └── loop
    │           ├── LoopApplication.java
    │           ├── domain
    │           │   ├── ad (광고)
    │           │   ├── auth (인증)
    │           │   ├── chat (채팅)
    │           │   ├── notification (알림)
    │           │   ├── post (게시글, 댓글, 좋아요, 신고)
    │           │   └── user (사용자, 마이페이지)
    │           └── global
    │               ├── common (공통 엔티티 및 에러 처리)
    │               ├── config (CORS, Security, Swagger, S3, WebSocket 설정)
    │               └── security (JWT, CustomUserDetailsService)
    └── resources
        ├── application.yml (애플리케이션 설정)
        ├── static
        └── templates


