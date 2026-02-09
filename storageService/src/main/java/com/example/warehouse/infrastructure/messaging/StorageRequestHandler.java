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
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "storage-requests", groupId = "storage-service-group")
    @SendTo
    public String handle(String rawRequest) {
        try {

            StorageRequest request = objectMapper.readValue(rawRequest, StorageRequest.class);

            log.debug("Storage request [{}]: id={}", request.getCorrelationId(), request.getStorageId());

            Storage storage = storageRepository.findById(request.getStorageId())
                    .orElse(null);


            StorageResponse response = new StorageResponse(
                    request.getCorrelationId(),
                    storage,
                    storage != null,
                    storage == null ? "Storage not found" : null
            );

                        return objectMapper.writeValueAsString(response);

        } catch (Exception e) {
            log.error("Error handling storage request", e);

                       try {
                StorageResponse errorResponse = new StorageResponse(
                        null,
                        null,
                        false,
                        "Internal error: " + e.getMessage()
                );
                return objectMapper.writeValueAsString(errorResponse);
            } catch (Exception jsonError) {

                return "{\"success\":false,\"error\":\"Fatal serialization error\"}";
            }
        }
    }
}