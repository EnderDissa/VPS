//package com.example.warehouse.config;
//
//import com.example.warehouse.infrastructure.messaging.dto.CountKeepingRequest;
//import com.example.warehouse.infrastructure.messaging.dto.CountKeepingResponse;
//import com.example.warehouse.infrastructure.messaging.dto.ItemRequest;
//import com.example.warehouse.infrastructure.messaging.dto.ItemResponse;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
//import org.springframework.kafka.listener.ContainerProperties;
//import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
//
//@Configuration
//public class KafkaConfig {
//
//    // Для запросов к сервису item (получение сущности)
//    @Bean
//    public ReplyingKafkaTemplate<String, ItemRequest, ItemResponse> itemReplyingKafkaTemplate(
//            ProducerFactory<String, ItemRequest> itemProducerFactory,
//            ConsumerFactory<String, ItemResponse> itemConsumerFactory) {
//
//        ContainerProperties props = new ContainerProperties("item-requests-responses");
//        return new ReplyingKafkaTemplate<>(
//                itemProducerFactory,
//                new ConcurrentMessageListenerContainer<>(itemConsumerFactory, props)
//        );
//    }
//
//    // Для запросов подсчёта (отдельный тип сообщения)
//    @Bean
//    public ReplyingKafkaTemplate<String, CountKeepingRequest, CountKeepingResponse> countKeepingsReplyingKafkaTemplate(
//            ProducerFactory<String, CountKeepingRequest> countProducerFactory,
//            ConsumerFactory<String, CountKeepingResponse> countConsumerFactory) {
//
//        ContainerProperties props = new ContainerProperties("keeping-count-requests-responses");
//        return new ReplyingKafkaTemplate<>(
//                countProducerFactory,
//                new ConcurrentMessageListenerContainer<>(countConsumerFactory, props)
//        );
//    }
//}
