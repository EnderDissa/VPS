//package com.example.warehouse.infrastructure.client;
//
//import com.example.warehouse.infrastructure.messaging.dto.UserRequest;
//import com.example.warehouse.infrastructure.messaging.dto.UserResponse;
//import com.example.warehouse.infrastructure.persistence.entity.User;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.common.header.internals.RecordHeader;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
//import org.springframework.kafka.requestreply.RequestReplyFuture;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class UserServiceClientKafka {
//    @Qualifier("userReplyingKafkaTemplate")
//    private final ReplyingKafkaTemplate<String, String, String> kafkaTemplate;
//    private final ObjectMapper objectMapper;
//
//    @Value("${kafka.topic.user.request:user-requests}")
//    private String requestTopic;
//
//    @Value("${kafka.topic.user.reply:user-requests-responses}")
//    private String replyTopic;
//
//    public Mono<User> getById(Long id) {
//        return Mono.fromCallable(() -> {
//                    String correlationId = UUID.randomUUID().toString();
//                    // 1. Создаем DTO-объект запроса
//                    UserRequest request = new UserRequest(correlationId, id);
//                    // 2. Сериализуем его в JSON-строку
//                    String requestPayload = objectMapper.writeValueAsString(request);
//
//                    ProducerRecord<String, String> record = new ProducerRecord<>(
//                            requestTopic, correlationId, requestPayload // Отправляем строку
//                    );
//                    record.headers().add(new RecordHeader(
//                            KafkaHeaders.REPLY_TOPIC,
//                            replyTopic.getBytes(StandardCharsets.UTF_8)
//                    ));
//                    record.headers().add(new RecordHeader(
//                            KafkaHeaders.CORRELATION_ID,
//                            correlationId.getBytes(StandardCharsets.UTF_8)
//                    ));
//
//                    RequestReplyFuture<String, String, String> future =
//                            kafkaTemplate.sendAndReceive(record);
//
//                    // 3. Получаем строку-ответ и десериализуем её в StorageResponse
//                    String rawResponse = future.get(5, TimeUnit.SECONDS).value();
//                    UserResponse response = objectMapper.readValue(rawResponse, UserResponse.class);
//
//                    // 4. Обрабатываем результат
//                    if (response.isSuccess() && response.getUser() != null) {
//                        return response.getUser();
//                    } else {
//                        throw new RuntimeException("User not found: " + response.getError());
//                    }
//                })
//                .timeout(Duration.ofSeconds(6))
//                .onErrorResume(e -> {
//                    log.error("Failed to fetch user id={}", id, e);
//                    return Mono.error(new RuntimeException("Failed to fetch user from service", e));
//                });
//    }
//}
