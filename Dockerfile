# 1단계: 빌드 스테이지 (무거운 작업은 깃허브 서버에서)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
# Gradle 사용 시 (Maven이면 ./mvnw clean package)
RUN ./gradlew clean bootJar

# 2단계: 실행 스테이지 (가벼운 작업만 Pi에서)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 빌드 스테이지에서 생성된 JAR만 복사
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 4000
ENTRYPOINT ["java", "-jar", "app.jar"]