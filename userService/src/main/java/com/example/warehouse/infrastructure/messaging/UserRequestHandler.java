//package com.example.warehouse.infrastructure.messaging;
//
//
//import com.example.warehouse.application.output.UserRepository;
//import com.example.warehouse.infrastructure.messaging.dto.UserRequest;
//import com.example.warehouse.infrastructure.messaging.dto.UserResponse;
//import com.example.warehouse.infrastructure.persistence.entity.User;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//import java.util.concurrent.CompletableFuture;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class UserRequestHandler {
//
//    private final UserRepository userRepository; // Реактивный репозиторий
//    private final ObjectMapper objectMapper;
//
//    @KafkaListener(topics = "user-requests", groupId = "user-service-group")
//    @SendTo
//    public CompletableFuture<String> handle(String rawRequest) { // Возвращаем CompletableFuture
//        log.debug("Received user request: {}", rawRequest);
//
//        return processRequest(rawRequest)
//                .timeout(Duration.ofSeconds(5)) // Таймаут на обработку
//                .onErrorResume(e -> handleError(rawRequest, e))
//                .doOnError(e -> log.error("Failed to process user request", e))
//                .toFuture(); // Конвертируем Mono в CompletableFuture
//    }
//
//    private Mono<String> processRequest(String rawRequest) {
//        return Mono.fromCallable(() -> {
//                    // Десериализуем синхронно (быстрая операция)
//                    UserRequest request = objectMapper.readValue(rawRequest, UserRequest.class);
//                    log.debug("Processing user request [{}]: id={}",
//                            request.getCorrelationId(), request.getUserId());
//                    return request;
//                })
//                .flatMap(request -> userRepository.findById(request.getUserId())
//                        .map(user -> createSuccessResponse(request, user))
//                        .switchIfEmpty(Mono.just(createNotFoundResponse(request)))
//                )
//                .flatMap(response -> Mono.fromCallable(() ->
//                        objectMapper.writeValueAsString(response)
//                ));
//    }
//
//    private UserResponse createSuccessResponse(UserRequest request, User user) {
//        return new UserResponse(
//                request.getCorrelationId(),
//                user,
//                true,
//                null
//        );
//    }
//
//    private UserResponse createNotFoundResponse(UserRequest request) {
//        return new UserResponse(
//                request.getCorrelationId(),
//                null,
//                false,
//                "User not found"
//        );
//    }
//
//    private Mono<String> handleError(String rawRequest, Throwable e) {
//        log.error("Error handling user request", e);
//
//        try {
//            // Пытаемся получить correlationId из запроса для ответа
//            UserRequest request = objectMapper.readValue(rawRequest, UserRequest.class);
//            UserResponse errorResponse = new UserResponse(
//                    request.getCorrelationId(),
//                    null,
//                    false,
//                    "Internal error: " + e.getMessage()
//            );
//            return Mono.just(objectMapper.writeValueAsString(errorResponse));
//        } catch (Exception jsonError) {
//            // Если не удалось десериализовать запрос
//            return Mono.just("{\"success\":false,\"error\":\"Fatal processing error\"}");
//        }
//    }
//}