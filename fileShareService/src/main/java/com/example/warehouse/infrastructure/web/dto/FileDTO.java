package com.example.warehouse.infrastructure.web.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private Long userId;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private byte[] fileContent;
}
