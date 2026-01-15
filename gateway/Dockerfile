FROM gradle:jdk17-alpine AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY build.gradle /home/gradle/java-code/
WORKDIR /home/gradle/java-code
RUN gradle clean build -i --stacktrace --no-daemon

FROM gradle:jdk17-alpine AS builder
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -i --stacktrace

FROM gradle:jdk17-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]