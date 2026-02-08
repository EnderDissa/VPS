package com.example.warehouse.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {

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
