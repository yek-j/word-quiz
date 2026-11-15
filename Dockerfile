# ─────────────────────────────────────────────────────────────
# 1단계: 빌드 (JDK 필요)
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# 의존성 정의만 먼저 복사한다.
# 소스만 바뀌었을 때 이 아래 레이어 캐시가 살아 있어 의존성 재다운로드를 건너뛴다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# 소스 복사 후 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ─────────────────────────────────────────────────────────────
# 2단계: 실행 (JRE만 — JDK/Gradle/소스가 이미지에 남지 않아 크기가 크게 줄고 공격 표면도 작아진다)
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# root로 실행하지 않는다. 컨테이너가 뚫려도 권한을 제한하기 위함.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080

# 컨테이너에 할당된 메모리(cgroup) 기준으로 힙을 잡게 한다.
# 기본값으로 두면 호스트 전체 메모리를 기준으로 계산해 OOM-kill 되기 쉽다.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
