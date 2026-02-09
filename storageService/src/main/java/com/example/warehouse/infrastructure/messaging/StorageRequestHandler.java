package com.example.warehouse.infrastructure.messaging;

import com.example.warehouse.application.output.StorageRepository;
import com.example.warehouse.infrastructure.messaging.dto.StorageRequest;
import com.example.warehouse.infrastructure.messaging.dto.StorageResponse;
import com.example.warehouse.infrastructure.persistence.entity.Storage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StorageRequestHandler {

    private final StorageRepository storageRepository;
    private final ObjectMapper objectMapper; // Добавляем ObjectMapper

    @KafkaListener(topics = "storage-requests", groupId = "storage-service-group")
    @SendTo
    public String handle(String rawRequest) { // Теперь принимаем String
        try {
            // 1. Десериализуем строку в объект StorageRequest
            StorageRequest request = objectMapper.readValue(rawRequest, StorageRequest.class);

            log.debug("Storage request [{}]: id={}", request.getCorrelationId(), request.getStorageId());

            Storage storage = storageRepository.findById(request.getStorageId())
                    .orElse(null);

            // 2. Создаем объект ответа
            StorageResponse response = new StorageResponse(
                    request.getCorrelationId(),
                    storage,
                    storage != null,
                    storage == null ? "Storage not found" : null
            );

            // 3. Сериализуем объект ответа в JSON-строку для возврата
            return objectMapper.writeValueAsString(response);

        } catch (Exception e) {
            log.error("Error handling storage request", e);

            // В случае ошибки тоже возвращаем JSON-строку
            try {
                StorageResponse errorResponse = new StorageResponse(
                        null, // correlationId может быть неизвестен
                        null,
                        false,
                        "Internal error: " + e.getMessage()
                );
                return objectMapper.writeValueAsString(errorResponse);
            } catch (Exception jsonError) {
                // Если даже сериализация ошибки не удалась
                return "{\"success\":false,\"error\":\"Fatal serialization error\"}";
            }
        }
    }
}