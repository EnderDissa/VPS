FROM gradle:jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

FROM gradle:jdk17-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]