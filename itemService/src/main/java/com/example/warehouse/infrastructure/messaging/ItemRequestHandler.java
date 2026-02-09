//package com.example.warehouse.infrastructure.messaging;
//
//import com.example.warehouse.application.ports.output.ItemRepository;
//import com.example.warehouse.application.ports.output.KeepingRepository;
//import com.example.warehouse.infrastructure.messaging.dto.*;
//import com.example.warehouse.infrastructure.persistence.entity.Item;
//import com.example.warehouse.infrastructure.persistence.entity.Storage;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class ItemRequestHandler {
//
//    private final ItemRepository itemRepository;
//    private final KeepingRepository keepingRepository;
//    private final ObjectMapper objectMapper;
//
//    // Обработчик получения товара
//    @KafkaListener(topics = "item-requests", groupId = "item-service-group")
//    @SendTo
//    public String handleIte(String rawRequest) {
//        try {
//            ItemRequest request = objectMapper.readValue(rawRequest, ItemRequest.class);
//
//            log.debug("Item request [{}]: id={}", request.getCorrelationId(), request.getItemId());
//
//            Item item = itemRepository.findById(request.getItemId())
//                    .orElse(null);
//
//            ItemResponse response = new ItemResponse(
//                    request.getCorrelationId(),
//                    item,
//                    item != null,
//                    item == null ? "item not found" : null
//            );
//
//            return objectMapper.writeValueAsString(response);
//
//        } catch (Exception e) {
//            log.error("Error handling item request", e);
//            try {
//                ItemResponse errorResponse = new ItemResponse(
//                        null, // correlationId может быть неизвестен
//                        null,
//                        false,
//                        "Internal error: " + e.getMessage()
//                );
//                return objectMapper.writeValueAsString(errorResponse);
//            } catch (Exception jsonError) {
//                // Если даже сериализация ошибки не удалась
//                return "{\"success\":false,\"error\":\"Fatal serialization error\"}";
//            }
//        }
//    }
//
//    // Обработчик подсчёта хранений
//    @KafkaListener(topics = "keeping-count-requests", groupId = "item-service-group")
//    @SendTo
//    public String handleCount(String rawRequest) {
//        try {
//            CountKeepingRequest request = objectMapper.readValue(rawRequest, CountKeepingRequest.class);
//
//            log.debug("CountKeeping request [{}]: id={}", request.getCorrelationId(), request.getStorageId());
//
//            Long count = keepingRepository.countByStorageId(request.getStorageId());
//
//            CountKeepingResponse response = new CountKeepingResponse(
//                    request.getCorrelationId(),
//                    count,
//                    true,
//                    null
//            );
//
//            return objectMapper.writeValueAsString(response);
//
//        } catch (Exception e) {
//            log.error("Error handling count keeping request", e);
//            try {
//                CountKeepingResponse errorResponse = new CountKeepingResponse(
//                        null, // correlationId может быть неизвестен
//                        null,
//                        false,
//                        "Internal error: " + e.getMessage()
//                );
//                return objectMapper.writeValueAsString(errorResponse);
//            } catch (Exception jsonError) {
//                // Если даже сериализация ошибки не удалась
//                return "{\"success\":false,\"error\":\"Fatal serialization error\"}";
//            }
//        }
//    }
//}
