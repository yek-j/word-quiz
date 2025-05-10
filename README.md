#  WordQuiz App (단어장)

## 엔티티 관계도 (ERD)
```
+-------------+       +-------------+       +----------------+       +--------------+
|    User     |<----->|    Quiz     |<----->| QuizWordBook   |<----->|  WordBook    |
+-------------+       +-------------+       +----------------+       +--------------+
| PK id       |       | PK id       |       | PK id          |       | PK id        |
| username    |       | name        |       | quizId(FK)     |       | name         |
| email       |       | description |       | wordBookId(FK) |       | description  |
| password    |       | sharingStatus|      +----------------+       | createdBy(FK)|
| createdAt   |       | createdBy(FK)|                               | createdAt    |
+-------------+       | createdAt   |                                +--------------+
       ^              +-------------+                                     ^
       |                     ^                                            |
       |                     |                                            |
       |              +-------------+       +----------------+            |
       +------------->| QuizSession |<----->|  QuizAnswer    |------------+
                      +-------------+       +----------------+       +-------------+
                      | PK id       |       | PK id          |<----->|    Word     |
                      | quizId(FK)  |       | sessionId(FK)  |       +-------------+
                      | userId(FK)  |       | wordId(FK)     |       | PK id       |
                      | score       |       | isCorrect      |       | term        |
                      | attemptedAt |       +----------------+       | description |
                      +-------------+                                | wordBookId  |
                                                                     | createdAt   |
                                                                     +-------------+
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

### Quiz (퀴즈)
| 필드명        | MySQL 타입    | Java 타입       | 제약조건                | 설명           |
|---------------|---------------|-----------------|-------------------------|----------------|
| id            | BIGINT        | Long            | PK, AUTO_INCREMENT     | 고유 식별자     |
| name          | VARCHAR(100)  | String          | UNIQUE, NOT NULL       | 퀴즈 이름       |
| sharingStatus | VARCHAR(20)   | Enum            | NOT NULL, DEFAULT 'PUBLIC' | 공유 상태    |
| description   | TEXT  | String          | NULL                   | 퀴즈 설명       |
| createdBy     | BIGINT        | Long            | FK (User.id), NOT NULL | 생성한 사용자   |
| createdAt     | DATETIME      | LocalDateTime   | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성 시간 |

### QuizWordBook (퀴즈-단어장 연결)
| 필드명      | MySQL 타입    | Java 타입       | 제약조건                | 설명           |
|-------------|---------------|-----------------|-------------------------|----------------|
| id          | BIGINT        | Long            | PK, AUTO_INCREMENT     | 고유 식별자     |
| quizId      | BIGINT        | Long            | FK (Quiz.id), NOT NULL | 퀴즈 ID        |
| wordBookId  | BIGINT        | Long            | FK (WordBook.id), NOT NULL | 단어장 ID   |

### QuizSession (퀴즈 세션)
| 필드명      | MySQL 타입    | Java 타입       | 제약조건                | 설명           |
|-------------|---------------|-----------------|-------------------------|----------------|
| id          | BIGINT        | Long            | PK, AUTO_INCREMENT     | 고유 식별자     |
| quizId      | BIGINT        | Long            | FK (Quiz.id), NOT NULL | 퀴즈 ID        |
| userId      | BIGINT        | Long            | FK (User.id), NOT NULL | 사용자 ID      |
| score       | INT           | Integer         | NOT NULL, DEFAULT 0    | 점수           |
| attemptedAt | DATETIME      | LocalDateTime   | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 시도 시간 |

### QuizAnswer (퀴즈 답변)
| 필드명        | MySQL 타입    | Java 타입       | 제약조건                | 설명           |
|---------------|---------------|-----------------|-------------------------|----------------|
| id            | BIGINT        | Long            | PK, AUTO_INCREMENT     | 고유 식별자     |
| quizSessionId | BIGINT        | Long            | FK (QuizSession.id), NOT NULL | 퀴즈 세션 ID |
| wordId        | BIGINT        | Long            | FK (Word.id), NOT NULL | 단어 ID        |
| isCorrect     | BIT(1)       | Boolean         | NULL                   | 정답 여부       |

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

| 엔드포인트                    | 메소드    | 설명          | 요청 데이터              | 인증 | 응답 데이터     |
|--------------------------|--------|-------------|---------------------|------|------------|
| `/api/v1/wordbooks`      | GET    | 사용자의 단어장 목록 | page, sort, orderby | Authorization 헤더 | 단어장 목록     |
| `/api/v1/wordbooks`      | POST   | 새 단어장 생성    | name, description   | Authorization 헤더 | 성공 메시지     |
| `/api/v1/wordbooks/{id}` | PUT    | 단어장 수정      | name, description   | Authorization 헤더 | 성공 메시지     |
| `/api/v1/wordbooks/{id}` | DELETE | 단어장 삭제      | -                   | Authorization 헤더 | 성공 메시지     |

### 단어 API (Words)
| 엔드포인트                                           | 메소드    | 설명         | 요청 데이터                     | 인증               | 응답 데이터           |
|-------------------------------------------------|--------|------------|----------------------------|------------------|------------------|
| `/api/v1/wordbooks/{wordBookId}/words`          | GET    | 단어장의 단어 목록 | page, sort, orderby        | Authorization 헤더 | 단어 목록            |
| `/api/v1/wordbooks/{wordBookId}/words`          | POST   | 단어 추가      | term, description          | Authorization 헤더 | 성공 메시지           |
| `/api/v1/wordbooks/{wordBookId}/words/{wordId}` | PUT    | 단어 수정      | 수정할 term, description, 단어장 | Authorization 헤더 | 성공 메시지           |
| `/api/v1/wordbooks/{wordBookId}/words/{wordId}` | DELETE | 단어 삭제      | -                          | Authorization 헤더 | 성공 메시지           |
| `/api/v1/wordbooks/{wordBookId}/words/duplicates` | POST   | 중복 단어 확인   | term                       | Authorization 헤더 | 중복 여부와 중복 단어, 설명 |
| `/api/v1/wordbooks/{wordBookId}/words/file` | POST   | 단어 파일 업로드   | 파일(Excel)                       | Authorization 헤더 | 처리 결과            |
| `/api/v1/wordbooks/{wordBookId}/words/template` | GET    | 단어 입력 템플릿 다운로드   | -                          | Authorization 헤더                | 템플릿(업로드 예시) 파일   |
