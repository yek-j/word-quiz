#  WordQuiz App (단어장)

## 테이블 상세 정의
### Users (사용자)
| 필드명    | MySQL 타입    | Java 타입       | 제약조건                          | 설명             |
|-----------|---------------|-----------------|-----------------------------------|------------------|
| id        | BIGINT        | Long            | PK, AUTO_INCREMENT                | 고유 식별자      |
| role      | VARCHAR(20)   | Enum(UserRole)  | NOT NULL, DEFAULT 'USER'          | 사용자 권한 (ADMIN, USER, QUIZ_MASTER) |
| username  | VARCHAR(50)   | String          | UNIQUE, NOT NULL                  | 사용자 이름      |
| email     | VARCHAR(100)  | String          | UNIQUE, NOT NULL                  | 이메일 주소      |
| password  | VARCHAR(255)  | String          | NOT NULL                          | 암호화된 비밀번호 |
| createdAt | DATETIME      | LocalDateTime   | NOT NULL                          | 계정 생성 시간   |

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
| 필드명        | MySQL 타입    | Java 타입       | 제약조건                                  | 설명           |
|---------------|---------------|-----------------|-------------------------------------------|----------------|
| id            | BIGINT        | Long            | PK, AUTO_INCREMENT                        | 고유 식별자     |
| name          | VARCHAR(100)  | String          | NOT NULL                                  | 퀴즈 이름       |
| sharingStatus | VARCHAR(20)   | Enum            | NOT NULL, DEFAULT 'PUBLIC'                | 공유 상태      |
| description   | TEXT          | String          | NULL                                      | 퀴즈 설명       |
| createdBy     | BIGINT        | Long            | FK (User.id), NOT NULL                    | 생성한 사용자   |
| createdAt     | DATETIME      | LocalDateTime   | NOT NULL, DEFAULT CURRENT_TIMESTAMP       | 생성 시간       |

### QuizWordBook (퀴즈-단어장 연결)
| 필드명      | MySQL 타입    | Java 타입       | 제약조건                | 설명           |
|-------------|---------------|-----------------|-------------------------|----------------|
| id          | BIGINT        | Long            | PK, AUTO_INCREMENT     | 고유 식별자     |
| quizId      | BIGINT        | Long            | FK (Quiz.id), NOT NULL | 퀴즈 ID        |
| wordBookId  | BIGINT        | Long            | FK (WordBook.id), NOT NULL | 단어장 ID   |

### QuizAllowedType (퀴즈-허용 퀴즈 타입 연결)
| 필드명      | MySQL 타입    | Java 타입       | 제약조건                          | 설명               |
|-------------|---------------|-----------------|-----------------------------------|--------------------|
| quiz_id     | BIGINT        | Long            | FK (Quiz.id), NOT NULL            | 퀴즈 ID            |
| quiz_type_id| BIGINT        | Long            | FK (QuizType.id), NOT NULL        | 퀴즈 타입 ID       |

### QuizType (퀴즈 타입)
| 필드명               | MySQL 타입   | Java 타입      | 제약조건                                  | 설명                      |
|----------------------|--------------|----------------|-------------------------------------------|---------------------------|
| id                   | BIGINT       | Long           | PK, AUTO_INCREMENT                        | 고유 식별자               |
| quizTypeName         | VARCHAR(50)  | String         | UNIQUE, NOT NULL                          | 퀴즈 타입 이름            |
| quizTypeDescription  | VARCHAR(500) | String         | NOT NULL                                  | 퀴즈 타입 설명            |
| useAi                | BIT(1)       | boolean        | NOT NULL, DEFAULT FALSE                   | AI 사용 여부              |
| createdAt            | DATETIME     | LocalDateTime  | NOT NULL                                  | 생성 시간                 |
| updatedAt            | DATETIME     | LocalDateTime  | NOT NULL                                  | 수정 시간                 |
| createdBy            | BIGINT       | Long           | NOT NULL                                  | 생성한 사용자 ID          |
| lastModifiedBy       | BIGINT       | Long           | NOT NULL                                  | 마지막 수정한 사용자 ID   |

> 기본 QuizType(`WORD_TO_MEANING`, `MEANING_TO_WORD`)은 `data.sql`로 자동 추가되며 삭제 차단됩니다.

### Prompt (AI 프롬프트)
| 필드명          | MySQL 타입   | Java 타입      | 제약조건                                  | 설명                          |
|-----------------|--------------|----------------|-------------------------------------------|-------------------------------|
| id              | BIGINT       | Long           | PK, AUTO_INCREMENT                        | 고유 식별자                   |
| promptTypeId    | BIGINT       | Long           | FK (QuizType.id), UNIQUE, NOT NULL        | 매핑된 퀴즈 타입 (1:1)        |
| promptName      | VARCHAR(50)  | String         | NOT NULL                                  | 프롬프트 이름                 |
| content         | TEXT         | String         | NOT NULL                                  | 프롬프트 본문                 |
| createdAt       | DATETIME     | LocalDateTime  | NOT NULL                                  | 생성 시간                     |
| updatedAt       | DATETIME     | LocalDateTime  | NOT NULL                                  | 수정 시간                     |
| createdBy       | BIGINT       | Long           | NOT NULL                                  | 생성한 사용자 ID              |
| lastModifiedBy  | BIGINT       | Long           | NOT NULL                                  | 마지막 수정한 사용자 ID       |

### Config (시스템 설정)
| 필드명               | MySQL 타입 | Java 타입 | 제약조건                          | 설명                          |
|----------------------|-----------|-----------|-----------------------------------|-------------------------------|
| id                   | BIGINT    | Long      | PK, AUTO_INCREMENT                | 고유 식별자                   |
| maxQuizCount         | INT       | int       | NOT NULL, 1~100                   | 사용자가 만들 수 있는 퀴즈 수 |
| maxWordBookCount     | INT       | int       | NOT NULL, 1~100                   | 사용자가 만들 수 있는 단어장 수 |
| maxWordBooksPerQuiz  | INT       | int       | NOT NULL, 1~100                   | 퀴즈 안 포함 가능한 단어장 수 |
| maxWordsPerBook      | INT       | int       | NOT NULL, 1~100                   | 단어장 안의 단어 수           |

### QuizSession (퀴즈 세션)
| 필드명         | MySQL 타입 | Java 타입       | 제약조건                                | 설명         |
|----------------|------------|-----------------|-----------------------------------------|--------------|
| id             | BIGINT     | Long            | PK, AUTO_INCREMENT                      | 고유 식별자  |
| quizId         | BIGINT     | Long            | FK (Quiz.id), NOT NULL                  | 퀴즈 ID      |
| userId         | BIGINT     | Long            | FK (User.id), NOT NULL                  | 사용자 ID    |
| score          | INT        | Integer         | NOT NULL, DEFAULT 0                     | 점수         |
| quizTypeId     | BIGINT     | Long            | FK (QuizType.id), NOT NULL              | 퀴즈 타입    |
| isQuizActive   | BIT(1)     | Boolean         | NOT NULL                                | 퀴즈 진행 여부 |
| attemptedAt    | DATETIME   | LocalDateTime   | NOT NULL, DEFAULT CURRENT_TIMESTAMP     | 시도 시간    |

### QuizQuestion (퀴즈 문제 메타데이터)
| 필드명                  | MySQL 타입 | Java 타입 | 제약조건                          | 설명                   |
|-------------------------|-----------|-----------|-----------------------------------|------------------------|
| id                      | BIGINT    | Long      | PK, AUTO_INCREMENT                | 고유 식별자            |
| quizSessionId           | BIGINT    | Long      | FK (QuizSession.id), NOT NULL     | 퀴즈 세션 ID           |
| wordId                  | BIGINT    | Long      | FK (Word.id), NOT NULL            | 단어 ID                |
| aiGeneratedSentence     | VARCHAR   | String    | NULL                              | AI 생성 문장 (AI 타입) |
| aiGeneratedTranslation  | VARCHAR   | String    | NULL                              | AI 생성 번역 (AI 타입) |
| questionOrder           | INT       | int       | NOT NULL                          | 단어 순서              |
| isCorrect               | BIT(1)    | Boolean   | NULL                              | 정답 여부              |

### LoginLog (로그인 기록)
| 필드명         | MySQL 타입 | Java 타입       | 제약조건                          | 설명             |
|----------------|------------|-----------------|-----------------------------------|------------------|
| id             | BIGINT     | Long            | PK, AUTO_INCREMENT                | 고유 식별자      |
| userId         | BIGINT     | Long            | NOT NULL                          | 사용자 ID        |
| userAgent      | TEXT       | String          | NULL                              | User-Agent 헤더  |
| userClientIp   | VARCHAR    | String          | NULL                              | 접속 IP          |
| loginAt        | DATETIME   | LocalDateTime   | NOT NULL                          | 로그인 시간      |

### UserConnection (사용자 관계 - 친구)
| 필드명              | Java 타입        | MySQL 타입   | 제약조건                              | 설명      |
|------------------|------------------|--------------|---------------------------------------|-----------|
| id               | Long             | BIGINT       | PK, AUTO_INCREMENT                    | 고유 식별자 |
| user             | User             | BIGINT       | FK(Users.id), NOT NULL                | 사용자  |
| targetUser       | User             | BIGINT       | FK(Users.id), NOT NULL                | 대상 사용자 |
| connectionType   | Enum(FRIEND, BLOCK) | VARCHAR(20) | NOT NULL                              | 관계 종류  |
| connectionStatus | Enum(PENDING, ACCEPTED, REJECTED) | VARCHAR(20) | NOT NULL               | 관계 상태  |
| createdAt        | LocalDateTime    | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP   | 생성 시간  |
| updatedAt        | LocalDateTime    | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP   | 상태 변경 시간 |

## API EndPoint

### 인증 API (Authentication)

| 엔드포인트 | 메소드    | 설명        | 요청 데이터                       | 인증 | 응답 데이터     |
|------------|--------|-----------|------------------------------|------|------------|
| `/api/v1/auth/signup` | POST   | 회원가입      | username, email, password    | - | 성공 메시지     |
| `/api/v1/auth/login` | POST   | 로그인       | email, password              | - | 토큰, 사용자 정보 |
| `/api/v1/auth/logout` | POST   | 로그아웃      | -                            | Authorization 헤더 | 성공 메시지     |
| `/api/v1/auth/refresh` | POST   | 액세스 토큰 갱신 | refreshToken (쿠키)          | - | 새 access token |
| `/api/v1/auth/me` | GET    | 현재 사용자 정보 | -                            | Authorization 헤더 | 사용자 정보     |
| `/api/v1/auth/password` | PUT    | 비밀번호 변경   | currentPassword, newPassword | Authorization 헤더 | 성공 메시지     |
| `/api/v1/auth/me` | DELETE | 사용자 삭제    | password                     | Authorization 헤더 | 성공 메시지     |
| `/api/v1/auth/me` | PUT | 사용자 수정    | username                     | Authorization 헤더 | 성공 메시지     |

### 사용자 API (Users)

| 엔드포인트 | 메소드 | 설명 | 요청 데이터 | 인증 | 응답 데이터 |
|------------|--------|------|-----------|------|-----------|
| `/api/v1/users/search` | GET | 사용자 검색 | username, page | Authorization 헤더 | 사용자 목록 |

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

### 퀴즈 API (Quizzes)
| 엔드포인트                                     | 메소드    | 설명             | 요청 데이터                                                           | 인증               | 응답 데이터                |
|-------------------------------------------|--------|----------------|------------------------------------------------------------------|------------------|-----------------------|
| `/api/v1/quizzes`                         | POST   | 퀴즈 생성          | name, description, WordBook List, sharingStatus, quizTypeIds     | Authorization 헤더 | 성공 메시지                |
| `/api/v1/quizzes`                         | GET    | 퀴즈 목록 조회       | page, sort, orderby, kind, searchId, quizTypeIds                  | Authorization 헤더 | 퀴즈 목록                 |
| `/api/v1/quizzes/types`                   | GET    | 사용 가능한 퀴즈 타입 조회 | -                                                                | Authorization 헤더 | 퀴즈 타입 목록             |
| `/api/v1/quizzes/{quizId}`                | GET    | 퀴즈 상세보기 조회     | -                                                                | Authorization 헤더 | 퀴즈 상세 정보             |
| `/api/v1/quizzes/{quizId}`                | PUT    | 퀴즈 수정          | name, description, WordBook List, sharingStatus, quizTypeIds     | Authorization 헤더 | 성공 메시지                |
| `/api/v1/quizzes/{quizId}`                | DELETE | 퀴즈 삭제          | -                                                                | Authorization 헤더 | 성공 메시지                |
| `/api/v1/quiz-session`                    | POST   | 퀴즈 시작          | quizId, quizTypeId                                                | Authorization 헤더 | 퀴즈 문제, 퀴즈 세션 정보 |
| `/api/v1/quiz-session/{sessionId}/answer` | POST   | 퀴즈 답변 제출       | wordId, answer                                                   | Authorization 헤더 | 채점 결과                 |
| `/api/v1/quiz-session/{sessionId}/result` | GET    | 퀴즈 결과          | -                                                                | Authorization 헤더 | 퀴즈 완료 결과            |

### 소셜 API (Social)
| 엔드포인트                                                    | 메소드    | 설명        | 요청 데이터       | 인증               | 응답 데이터     |
|------------------------------------------------------------|--------|-----------|--------------|------------------|------------|
| `/api/v1/social/friend-requests`                           | POST   | 친구 요청     | friendUserName | Authorization 헤더 | 성공/실패 메시지  |
| `/api/v1/social/friend-requests`                           | GET    | 친구 요청 목록  | page          | Authorization 헤더 | 요청 사용자 목록  |
| `/api/v1/social/friend-requests/{requestUserId}/accept`    | POST   | 친구 요청 수락  | -             | Authorization 헤더 | 성공 메시지     |
| `/api/v1/social/friend-requests/{requestUserId}/reject`    | POST   | 친구 요청 거절  | -             | Authorization 헤더 | 성공 메시지     |
| `/api/v1/social/friends`                                   | GET    | 친구 목록 조회  | page          | Authorization 헤더 | 친구 목록      |
| `/api/v1/social/friends/{friendId}`                        | DELETE | 친구 삭제     | -             | Authorization 헤더 | 성공 메시지     |

### 분석 API (Analysis)
| 엔드포인트                                    | 메소드    | 설명        | 요청 데이터                             | 인증               | 응답 데이터          |
|------------------------------------------|--------|-----------|------------------------------------|------------------|-----------------|
| `/api/v1/analysis/quiz`                  | GET   | 퀴즈별 통계 분석 | -                                  | Authorization 헤더 | 퀴즈별 성과 통계          |
| `/api/v1/analysis/weak-words`            | GET    | 취약 단어 분석  | limit(기본값:10), maxAccuracy(기본값:50) | Authorization 헤더 | 취약 단어 목록과 통계           |
| `/api/v1/analysis/overview`              | GET    | 전체 학습 현황     | -                                  | Authorization 헤더 | 학습 개요 통계          |

### 관리자 API (Admin)
> 모든 엔드포인트는 ADMIN 권한 필요 (Authorization 헤더)

| 엔드포인트                                       | 메소드    | 설명                          | 요청 데이터                                                                | 응답 데이터       |
|--------------------------------------------|--------|-----------------------------|-----------------------------------------------------------------------|--------------|
| `/api/v1/admin/users`                      | GET    | 전체 사용자 목록 조회                | username, page, orderby, sort                                          | 사용자 목록     |
| `/api/v1/admin/users/{userId}/role`        | PATCH  | 사용자 권한 변경                   | role                                                                  | 성공 메시지    |
| `/api/v1/admin/quizType`                   | POST   | 퀴즈 타입 추가                   | quizTypeName, quizTypeDescription, useAi                              | 성공 메시지    |
| `/api/v1/admin/quizType`                   | GET    | 퀴즈 타입 목록 조회                | quizTypeName, page, orderby, sort                                     | 퀴즈 타입 목록 |
| `/api/v1/admin/quizType/{quizTypeId}`      | GET    | 퀴즈 타입 단건 조회                | -                                                                     | 퀴즈 타입 상세 |
| `/api/v1/admin/quizType/{quizTypeId}`      | PUT    | 퀴즈 타입 수정                   | quizTypeName, quizTypeDescription, useAi                              | 성공 메시지    |
| `/api/v1/admin/quizType/{quizTypeId}`      | DELETE | 퀴즈 타입 삭제 (기본 타입 삭제 차단)     | -                                                                     | 성공 메시지    |
| `/api/v1/admin/prompt/validate`            | POST   | 프롬프트 검증 (LLM 구조화 출력 테스트)   | content                                                               | 검증 결과     |
| `/api/v1/admin/prompt`                     | POST   | 프롬프트 추가                    | promptName, content, promptTypeId                                     | 성공 메시지    |
| `/api/v1/admin/prompt`                     | GET    | 프롬프트 목록 조회                 | promptName, promptType, page, orderby, sort                           | 프롬프트 목록  |
| `/api/v1/admin/prompt/{promptId}`          | GET    | 프롬프트 단건 조회                 | -                                                                     | 프롬프트 상세  |
| `/api/v1/admin/prompt/{promptId}`          | PUT    | 프롬프트 수정                    | promptName, content, promptTypeId                                     | 성공 메시지    |
| `/api/v1/admin/prompt/{promptId}`          | DELETE | 프롬프트 비활성화                  | -                                                                     | 성공 메시지    |
| `/api/v1/admin/config`                     | PUT    | 시스템 설정 수정                  | maxQuizCount, maxWordBookCount, maxWordBooksPerQuiz, maxWordsPerBook  | 성공 메시지    |
