FROM arm64v8/gradle:jdk17-noble AS gradle-cache
WORKDIR /home/gradle/src
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

FROM arm64v8/gradle:jdk17-noble AS builder
WORKDIR /app
COPY --from=gradle-cache /home/gradle/.gradle /home/gradle/.gradle
COPY . .
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle clean bootJar --no-daemon

FROM arm64v8/eclipse-temurin:17-noble
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]