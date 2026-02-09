FROM gradle:jdk17-alpine AS gradle-cache
WORKDIR /home/gradle/src
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

FROM gradle:jdk17-alpine AS builder
WORKDIR /app
COPY --from=gradle-cache /home/gradle/.gradle /home/gradle/.gradle
COPY . .
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle clean bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]