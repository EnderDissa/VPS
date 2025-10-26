FROM gradle:8.0-jdk17 AS build

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle clean build --no-daemon

# Stage 2: Run
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]