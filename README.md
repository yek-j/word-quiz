#  WordQuiz App (단어장)

## 엔티티 관계도 (ERD)
```
+-------------+       +--------------+       +-------------+
|    User     |       |   WordBook   |       |    Word     |
+-------------+       +--------------+       +-------------+
| PK id       |<----->| PK id        |<----->| PK id       |
| username    |       | name         |       | term        |
| email       |       | description  |       | description |
| password    |       | createdBy(FK)|       | wordBookId  |
| createdAt   |       | createdAt    |       | createdAt   |
+-------------+       +--------------+       +-------------+
```

## 테이블 상세 정의

### Users (사용자)
| 필드명    | MySQL 타입    | Java 타입       | 제약조건                | 설명           |
|-----------|---------------|-----------------|-------------------------|----------------|
| id        | BIGINT        | Long            | PK, AUTO_INCREMENT     | 고유 식별자     |
| username  | VARCHAR(50)   | String          | UNIQUE, NOT NULL       | 사용자 이름     |
| email     | VARCHAR(100)  | String          | UNIQUE, NOT NULL       | 이메일 주소     |
| password  | VARCHAR(255)  | String          | NOT NULL               | 암호화된 비밀번호|
| createdAt | DATETIME      | LocalDateTime   | NOT NULL               | 계정 생성 시간  |

### WordBooks (단어장)
| 필드명      | MySQL 타입    | Java 타입       | 제약조건                | 설명           |
|-------------|---------------|-----------------|-------------------------|----------------|
| id          | BIGINT        | Long            | PK, AUTO_INCREMENT     | 고유 식별자     |
| name        | VARCHAR(100)  | String          | NOT NULL               | 단어장 이름     |
| description | VARCHAR(255)  | String          | NULL                   | 단어장 설명     |
| createdBy   | BIGINT        | Long            | FK (User.id), NOT NULL | 생성한 사용자   |
| createdAt   | DATETIME      | LocalDateTime   | NOT NULL               | 생성 시간       |

### Word (단어)
| 필드명      | MySQL 타입    | Java 타입       | 제약조건                | 설명           |
|-------------|---------------|-----------------|-------------------------|----------------|
| id          | BIGINT        | Long            | PK, AUTO_INCREMENT     | 고유 식별자     |
| wordBookId  | BIGINT        | Long            | FK (WordBook.id), NOT NULL | 소속 단어장  |
| term        | VARCHAR(100)  | String          | NOT NULL               | 단어           |
| description | TEXT          | String          | NOT NULL               | 단어 의미/정의  |
| createdAt   | DATETIME      | LocalDateTime   | NOT NULL               | 생성시간        |

## API EndPoint

### 인증 API (Authentication)

| 엔드포인트 | 메소드    | 설명        | 요청 데이터                       | 인증 | 응답 데이터     |
|------------|--------|-----------|------------------------------|------|------------|
| `/api/v1/auth/signup` | POST   | 회원가입      | username, email, password    | - | 성공 메시지     |
| `/api/v1/auth/login` | POST   | 로그인       | email, password              | - | 토큰, 사용자 정보 |
| `/api/v1/auth/logout` | POST   | 로그아웃      | -                            | Authorization 헤더 | 성공 메시지     |
| `/api/v1/auth/me` | GET    | 현재 사용자 정보 | -                            | Authorization 헤더 | 사용자 정보     |
| `/api/v1/auth/password` | PUT    | 비밀번호 변경   | currentPassword, newPassword | Authorization 헤더 | 성공 메시지     |
| `/api/v1/auth/me` | DELETE | 사용자 삭제    | password                     | Authorization 헤더 | 성공 메시지     |

### 단어장 API (WordBooks)

| 엔드포인트 | 메소드    | 설명        | 요청 데이터                       | 인증 | 응답 데이터     |
|------------|--------|-----------|------------------------------|------|------------|
| `/api/v1/wordbooks` | POST   | 새 단어장 생성      | name, description    | Authorization 헤더 | 성공 메시지     |

### 단어 API (Words)
| 엔드포인트 | 메소드    | 설명    | 요청 데이터            | 인증 | 응답 데이터     |
|------------|--------|-------|-------------------|------|------------|
| `/api/v1/wordbooks/{wordBookId}/words` | POST   | 단어 추가 | term, description | Authorization 헤더 | 성공 메시지     |
