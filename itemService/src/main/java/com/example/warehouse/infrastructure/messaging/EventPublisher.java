//package com.example.warehouse.infrastructure.messaging;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.SendResult;
//import org.springframework.stereotype.Component;
//import java.util.concurrent.CompletableFuture;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class EventPublisher {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    // Публикация Item событий
//    public CompletableFuture<Void> publishItemEvent(ItemEvent event) {
//        return publish("item-events", event.getAggregateId(), event);
//    }
//
////    // Публикация Borrowing событий
////    public CompletableFuture<Void> publishBorrowingEvent(BorrowingEvent event) {
////        return publish("borrowing-events", event.getAggregateId(), event);
////    }
//
////    // Публикация Maintenance событий
////    public CompletableFuture<Void> publishMaintenanceEvent(MaintenanceEvent event) {
////        return publish("maintenance-events", event.getAggregateId(), event);
////    }
////
////    // Публикация Keeping событий
////    public CompletableFuture<Void> publishKeepingEvent(KeepingEvent event) {
////        return publish("keeping-events", event.getAggregateId(), event);
////    }
////
////    // Уведомления
////    public CompletableFuture<Void> publishNotification(String key, NotificationEvent event) {
////        return publish("notifications", key, event);
////    }
//
//    private CompletableFuture<Void> publish(String topic, String key, Object event) {
//        CompletableFuture<SendResult<String, Object>> sendFuture = kafkaTemplate.send(topic, key, event);
//
//        return sendFuture.handle((result, ex) -> {
//            if (ex == null) {
//                log.debug("Event published successfully to topic {}: {}", topic, event.getClass().getSimpleName());
//                return null;
//            } else {
//                log.error("Failed to publish event to topic {}: {}", topic, ex.getMessage(), ex);
//                // Прокидываем исключение, чтобы внешний CompletableFuture завершился с ошибкой
//                throw new RuntimeException("Failed to publish Kafka event", ex);
//            }
//        });
//    }
//
//    // Пакетная публикация
//    public void publishEvents(String topic, String key, Object... events) {
//        for (Object event : events) {
//            publish(topic, key, event);
//        }
//    }
//}