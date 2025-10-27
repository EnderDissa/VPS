# Build stage (if you want to build inside Docker)
# Alternatively, you can build JAR locally with ./gradlew bootJar

FROM openjdk:17-jdk-slim AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar --no-daemon

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]