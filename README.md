현재 Readme 정리중입니다.

# OHNEW BE Server
구름톤 유니브 시즌톤 67팀 오뉴 백엔드 레포지토리
<img width="1530" height="855" alt="image" src="https://github.com/user-attachments/assets/8eb7371e-c0cc-4358-855f-289eccb491e9" />


# 아키텍처 구조도(셀프 호스팅)

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/d4cc1a25-e5a1-4d97-a713-becc94830032" />


## 📌 Tech Stack

| 구분              | 기술 스택 |
|-------------------|-----------|
| **Back-End**      | - Spring Boot (Spring Security, JWT, OAuth2.0, Spring Data JPA - Hibernate)<br>- FastAPI (OpenAI ChatGPT API 연동) |
| **Database / Storage** | - MySQL <br> - Redis |
| **인증**          | - 카카오 로그인 <br> - JWT |
| **배포 / 운영**   | - Caddy (HTTPS) <br> - GitHub + GitHub Actions <br> - Gradle <br> - Nssm (Caddy, Spring, FastAPI 백그라운드 서비스 등록 및 자동 실행) |
| **개발 환경**     | Local Developer → GitHub → GitHub Actions (Runners) → 배포 |


# 프로젝트 구조
---
'''

src
└── main
    ├── java
    │   └── com
    │       └── ohnew
    │           └── ohnew
    │               ├── apiPayload
    │               │   ├── code
    │               │   │   ├── exception   # 커스텀 예외 처리
    │               │   │   └── status      # 에러 코드 정의
    │               │   └── response        # 공통 응답 DTO
    │               ├── common              # 공통 유틸/설정 (예: PythonApi, Swagger, Security 등)
    │               │   ├── config
    │               │   └── security
    │               ├── controller
    │               ├── converter
    │               ├── dto
    │               ├── entity
    │               ├── global
    │               │   ├── controller
    │               │   └── util
    │               ├── repository
    │               └── service
    ├── resource


         
'''
---

