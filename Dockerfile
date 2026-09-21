# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 의존성 레이어 캐시: 빌드 설정만 먼저 복사해서 받아둔다
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew \
    && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src src
RUN ./gradlew --no-daemon bootJar -x test \
    && mv "$(ls build/libs/*.jar | grep -v -- '-plain.jar')" app.jar

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --create-home --uid 1001 app \
    && mkdir -p /app/uploads && chown -R app:app /app
COPY --from=build --chown=app:app /workspace/app.jar app.jar

USER app
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -Duser.timezone=Asia/Seoul"

# 업로드 이미지는 볼륨으로 유지
VOLUME /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
