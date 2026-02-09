package com.example.warehouse.application.ports.input.interfaces;

import com.example.warehouse.domain.model.File;
import java.util.List;

public interface FileService {
    File uploadFile(String fileName, String originalFileName, String contentType, Long fileSize, byte[] fileContent, Long userId);
    File getFileById(Long id, Long userId, boolean isAdmin);
    List<File> getUserFiles(Long userId, int page, int size, boolean isAdmin);
    void deleteFile(Long id, Long userId, boolean isAdmin);
    long getFileCount(Long userId, boolean isAdmin);
    byte[] getFileContent(Long id, Long userId, boolean isAdmin) throws java.io.IOException;
}
