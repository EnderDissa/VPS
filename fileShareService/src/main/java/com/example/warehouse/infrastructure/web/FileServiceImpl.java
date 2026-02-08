package com.example.warehouse.infrastructure.web;

import com.example.warehouse.application.ports.input.interfaces.FileService;
import com.example.warehouse.application.ports.output.FileRepository;
import com.example.warehouse.domain.model.File;
import com.example.warehouse.exception.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    // Setter for testing
    public void setFileRepository(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public File uploadFile(String fileName, String originalFileName, String contentType, Long fileSize, byte[] fileContent, Long userId) {
        // Generate a unique file name to avoid conflicts
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        
        File file = File.builder()
            .fileName(uniqueFileName)
            .originalFileName(originalFileName)
            .contentType(contentType)
            .fileSize(fileSize)
            .fileContent(fileContent) // Store file content in the database
            .userId(userId)
            .uploadedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
            
        return fileRepository.save(file);
    }

    @Override
    public File getFileById(Long id, Long userId, boolean isAdmin) {
        File file = fileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
            
        // Check access permissions
        if (!isAdmin && !file.getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to access this file");
        }
        
        return file;
    }

    @Override
    public List<File> getUserFiles(Long userId, int page, int size, boolean isAdmin) {
        if (isAdmin) {
            // Admin can see all files
            return fileRepository.findAll(page, size);
        } else {
            // Regular user can only see their own files
            return fileRepository.findByUserId(userId, page, size);
        }
    }

    @Override
    public void deleteFile(Long id, Long userId, boolean isAdmin) {
        File file = getFileById(id, userId, isAdmin); // This also checks permissions
        fileRepository.deleteById(id);
    }

    @Override
    public long getFileCount(Long userId, boolean isAdmin) {
        if (isAdmin) {
            return fileRepository.countAll();
        } else {
            return fileRepository.countByUserId(userId);
        }
    }
    
    // Method to get file content by ID
    @Override
    public byte[] getFileContent(Long id, Long userId, boolean isAdmin) throws IOException {
        File file = getFileById(id, userId, isAdmin);
        return file.getFileContent();
    }
}
