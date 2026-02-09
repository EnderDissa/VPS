//package com.example.warehouse.infrastructure.client;
//
//import com.example.warehouse.infrastructure.messaging.dto.CountKeepingRequest;
//import com.example.warehouse.infrastructure.messaging.dto.CountKeepingResponse;
//import com.example.warehouse.infrastructure.messaging.dto.ItemRequest;
//import com.example.warehouse.infrastructure.messaging.dto.ItemResponse;
//import com.example.warehouse.infrastructure.persistence.entity.Item;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.common.header.internals.RecordHeader;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Primary;
//import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
//import org.springframework.kafka.requestreply.RequestReplyFuture;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.util.UUID;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//// infrastructure/client/ItemServiceKafkaClient.java
//@Component
//@Primary // Гарантирует, что будет использоваться эта реализация при @Autowired
//@RequiredArgsConstructor
//@Slf4j
//public class ItemServiceKafkaClient{
//
//    private final ReplyingKafkaTemplate<String, ItemRequest, ItemResponse> itemKafkaTemplate;
//    private final ReplyingKafkaTemplate<String, CountKeepingRequest, CountKeepingResponse> countKafkaTemplate;
//
//    @Value("${kafka.topic.item.request:item-requests}")
//    private String itemRequestTopic;
//
//    @Value("${kafka.topic.item.reply:item-requests-responses}")
//    private String itemReplyTopic;
//
//    @Value("${kafka.topic.keeping.count.request:keeping-count-requests}")
//    private String countRequestTopic;
//
//    @Value("${kafka.topic.keeping.count.reply:keeping-count-requests-responses}")
//    private String countReplyTopic;
//
//    public Mono<Item> getItemById(Long id) {
//        String correlationId = UUID.randomUUID().toString();
//        ItemRequest request = new ItemRequest(correlationId, id);
//
//        ProducerRecord<String, ItemRequest> record = new ProducerRecord<>(
//                itemRequestTopic, correlationId, request
//        );
//        addReplyHeaders(record, itemReplyTopic, correlationId);
//
//        RequestReplyFuture<String, ItemRequest, ItemResponse> future =
//                itemKafkaTemplate.sendAndReceive(record);
//
//        return Mono.fromCallable(() -> getResponseWithTimeout(future, id))
//                .flatMap(response ->
//                        response.isSuccess() && response.getItem() != null
//                                ? Mono.just(response.getItem())
//                                : Mono.error(new RuntimeException("Item not found: " + response.getError()))
//                )
//                .timeout(Duration.ofSeconds(6))
//                .doOnError(e -> log.error("Failed to fetch item id={}: {}", id, e.getMessage()));
//    }
//
//    public Mono<Long> countKeepingsByStorageId(Long storageId) {
//        String correlationId = UUID.randomUUID().toString();
//        CountKeepingRequest request = new CountKeepingRequest(correlationId, storageId);
//
//        ProducerRecord<String, CountKeepingRequest> record = new ProducerRecord<>(
//                countRequestTopic, correlationId, request
//        );
//        addReplyHeaders(record, countReplyTopic, correlationId);
//
//        RequestReplyFuture<String, CountKeepingRequest, CountKeepingResponse> future =
//                countKafkaTemplate.sendAndReceive(record);
//
//        return Mono.fromCallable(() -> getCountResponseWithTimeout(future, storageId))
//                .flatMap(response ->
//                        response.isSuccess()
//                                ? Mono.just(response.getCount())
//                                : Mono.error(new RuntimeException("Count failed: " + response.getError()))
//                )
//                .timeout(Duration.ofSeconds(6))
//                .doOnError(e -> log.error("Failed to count keepings for storage id={}: {}", storageId, e.getMessage()));
//    }
//
//    // --- Вспомогательные методы ---
//
//    private void addReplyHeaders(ProducerRecord<String, ?> record, String replyTopic, String correlationId) {
//        record.headers().add(new RecordHeader(
//                KafkaHeaders.REPLY_TOPIC,
//                replyTopic.getBytes(StandardCharsets.UTF_8)
//        ));
//        record.headers().add(new RecordHeader(
//                KafkaHeaders.CORRELATION_ID,
//                correlationId.getBytes(StandardCharsets.UTF_8)
//        ));
//    }
//
//    private ItemResponse getResponseWithTimeout(
//            RequestReplyFuture<String, ItemRequest, ItemResponse> future, Long id) {
//        try {
//            return future.get(5, TimeUnit.SECONDS).value();
//        } catch (TimeoutException e) {
//            throw new RuntimeException("Kafka timeout for item id=" + id, e);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("Kafka request interrupted for item id=" + id, e);
//        } catch (ExecutionException e) {
//            throw new RuntimeException("Kafka execution error for item id=" + id,
//                    e.getCause() != null ? e.getCause() : e);
//        }
//    }
//
//    private CountKeepingResponse getCountResponseWithTimeout(
//            RequestReplyFuture<String, CountKeepingRequest, CountKeepingResponse> future, Long storageId) {
//        try {
//            return future.get(5, TimeUnit.SECONDS).value();
//        } catch (TimeoutException e) {
//            throw new RuntimeException("Kafka timeout for storage id=" + storageId, e);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("Kafka request interrupted for storage id=" + storageId, e);
//        } catch (ExecutionException e) {
//            throw new RuntimeException("Kafka execution error for storage id=" + storageId,
//                    e.getCause() != null ? e.getCause() : e);
//        }
//    }
//}
