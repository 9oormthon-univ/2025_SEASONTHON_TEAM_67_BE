# OHNEW BE
구름톤 유니브 시즌톤 67팀 오뉴 백엔드 레포지토리
<img width="1530" height="855" alt="image" src="https://github.com/user-attachments/assets/8eb7371e-c0cc-4358-855f-289eccb491e9" />

# Introduction
양질의 콘텐츠에 중독되다! 숏폼으로 중독되는 뉴스, 뉴스를 보는 새로운 방법

# Architectural Structural Chart (Self-Hosting)
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/d4cc1a25-e5a1-4d97-a713-becc94830032" />


## 📌 Tech Stack

| 구분              | 기술 스택 |
|-------------------|-----------|
| **Back-End**      | - Spring Boot (Spring Security, JWT, OAuth2.0, Spring Data JPA - Hibernate)<br>- FastAPI (OpenAI ChatGPT API 연동) |
| **Database / Storage** | - MySQL <br> - Redis |
| **인증**          | - 카카오 로그인 <br> - JWT |
| **배포 / 운영**   | - Caddy (HTTPS) <br> - GitHub + GitHub Actions <br> - Gradle <br> - Nssm (Caddy, Spring, FastAPI 백그라운드 서비스 등록 및 자동 실행) |
| **개발 환경**     | Local Developer → GitHub → GitHub Actions (Runners) → 배포 |


# Project Struct
---
<pre>
src
├── main
│   ├── java
│   │   └── com
│   │       └── ohnew
│   │           └── ohnew
│   │               ├── OhnewApplication.java     # 메인 실행 클래스
│   │               ├── apiPayload
│   │               │   ├── code
│   │               │   │   ├── exception         # 커스텀 예외 처리
│   │               │   │   └── status            # 에러 코드 정의
│   │               │   └── ApiResponse.java      # 공통 응답 DTO
│   │               ├── common
│   │               │   ├── config                # 설정 (Async, Redis, Swagger, WebClient 등)
│   │               │   └── security              # 보안 (JWT, 필터, SecurityConfig 등)
│   │               │       └── handler           # Security 관련 핸들러
│   │               ├── controller                # REST 컨트롤러
│   │               │   ├── ChatController.java
│   │               │   ├── NewsController.java
│   │               │   ├── SocialLoginController.java
│   │               │   ├── SocialLoginPageController.java
│   │               │   └── UserController.java
│   │               ├── converter                 # DTO ↔ Entity 변환
│   │               │   ├── ChatConverter.java
│   │               │   ├── NewsConverter.java
│   │               │   └── UserConverter.java
│   │               ├── dto
│   │               │   ├── req                   # 요청 DTO
│   │               │   │   ├── ChatbotReq.java
│   │               │   │   ├── TokenDtoReq.java
│   │               │   │   ├── UserDtoReq.java
│   │               │   │   └── UserPreferenceDtoReq.java
│   │               │   └── res                   # 응답 DTO
│   │               │       ├── ChatDtoRes.java
│   │               │       ├── KakaoTokenResponseDto.java
│   │               │       ├── KakaoUserInfoResponseDto.java
│   │               │       ├── NewsByMultiRssRes.java
│   │               │       ├── NewsByPythonRes.java
│   │               │       ├── NewsByRssRes.java
│   │               │       ├── NewsDtoRes.java
│   │               │       ├── UserDtoRes.java
│   │               │       └── UserPreferenceDtoRes.java
│   │               ├── entity
│   │               │   ├── enums
│   │               │   │   ├── ChatSender.java
│   │               │   │   ├── NewsStyle.java
│   │               │   │   └── Provider.java
│   │               │   ├── ChatMessage.java
│   │               │   ├── ChatRoom.java
│   │               │   ├── News.java
│   │               │   ├── NewsSummaryVariant.java
│   │               │   ├── Scrap.java
│   │               │   ├── User.java
│   │               │   └── UserPreference.java
│   │               ├── global
│   │               │   ├── controller
│   │               │   │   ├── HealthController.java
│   │               │   │   └── TimeCheckController.java
│   │               │   └── util
│   │               │       └── CookieUtil.java
│   │               ├── repository                # JPA Repository
│   │               │   ├── ChatMessageRepository.java
│   │               │   ├── ChatRoomRepository.java
│   │               │   ├── NewsRepository.java
│   │               │   ├── NewsSummaryVariantRepository.java
│   │               │   ├── ScrapRepository.java
│   │               │   ├── UserPreferenceRepository.java
│   │               │   └── UserRepository.java
│   │               └── service                   # 비즈니스 로직
│   │                   ├── ChatService.java
│   │                   ├── ChatServiceImpl.java
│   │                   ├── KakaoService.java
│   │                   ├── NewsService.java
│   │                   ├── NewsServiceImpl.java
│   │                   ├── NewsVariantService.java
│   │                   ├── RssServiceImpl.java
│   │                   ├── UserPreferenceService.java
│   │                   ├── UserService.java
│   │                   └── UserServiceImpl.java
│   └── resources
│       ├── application.yml
│       ├── application-deploy.yml   # DB, Redis, OAuth2.0 환경변수로 적용해둔 설정 파일
│       ├── static
│       └── templates
│
└── test
</pre>
---

