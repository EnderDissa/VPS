package com.example.warehouse.infrastructure.client;

import com.example.warehouse.infrastructure.messaging.dto.StorageRequest;
import com.example.warehouse.infrastructure.messaging.dto.StorageResponse;
import com.example.warehouse.infrastructure.persistence.entity.Storage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
@Slf4j
public class StorageServiceClient {

    private final ReplyingKafkaTemplate<String, String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.storage.request:storage-requests}")
    private String requestTopic;

    @Value("${kafka.topic.storage.reply:storage-requests-responses}")
    private String replyTopic;

    public Mono<Storage> getById(Long id) {
        return Mono.fromCallable(() -> {
                    String correlationId = UUID.randomUUID().toString();

                    StorageRequest request = new StorageRequest(correlationId, id);

                    String requestPayload = objectMapper.writeValueAsString(request);

                    ProducerRecord<String, String> record = new ProducerRecord<>(
                            requestTopic, correlationId, requestPayload
                    );
                    record.headers().add(new RecordHeader(
                            KafkaHeaders.REPLY_TOPIC,
                            replyTopic.getBytes(StandardCharsets.UTF_8)
                    ));
                    record.headers().add(new RecordHeader(
                            KafkaHeaders.CORRELATION_ID,
                            correlationId.getBytes(StandardCharsets.UTF_8)
                    ));

                    RequestReplyFuture<String, String, String> future =
                            kafkaTemplate.sendAndReceive(record);


                    String rawResponse = future.get(5, TimeUnit.SECONDS).value();
                    StorageResponse response = objectMapper.readValue(rawResponse, StorageResponse.class);


                    if (response.isSuccess() && response.getStorage() != null) {
                        return response.getStorage();
                    } else {
                        throw new RuntimeException("Storage not found: " + response.getError());
                    }
                })
                .timeout(Duration.ofSeconds(6))
                .onErrorResume(e -> {
                    log.error("Failed to fetch storage id={}", id, e);
                    return Mono.error(new RuntimeException("Failed to fetch storage from service", e));
                });
    }
}
