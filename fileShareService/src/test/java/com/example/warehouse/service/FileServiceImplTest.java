 package com.example.warehouse.service;

import com.example.warehouse.application.ports.input.interfaces.FileService;
import com.example.warehouse.application.ports.output.FileRepository;
import com.example.warehouse.domain.model.File;
import com.example.warehouse.exception.AccessDeniedException;
import com.example.warehouse.infrastructure.web.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileServiceImplTest {

    private FileServiceImpl fileService;

    @Mock
    private FileRepository fileRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileService = new FileServiceImpl();
        fileService.setFileRepository(fileRepository);
    }

    @Test
    void uploadFile_shouldCreateAndSaveFile() {
        // Given
        String fileName = "test.txt";
        String originalFileName = "test.txt";
        String contentType = "text/plain";
        Long fileSize = 100L;
        byte[] fileContent = "test content".getBytes();
        Long userId = 1L;
        
        File savedFile = File.builder()
            .id(1L)
            .fileName(anyString())
            .originalFileName(originalFileName)
            .contentType(contentType)
            .fileSize(fileSize)
            .fileContent(fileContent)
            .userId(userId)
            .uploadedAt(any(LocalDateTime.class))
            .updatedAt(any(LocalDateTime.class))
            .build();
        
        when(fileRepository.save(any(File.class))).thenReturn(savedFile);

        // When
        File result = fileService.uploadFile(fileName, originalFileName, contentType, fileSize, fileContent, userId);

        // Then
        assertNotNull(result);
        assertEquals(originalFileName, result.getOriginalFileName());
        assertEquals(contentType, result.getContentType());
        assertEquals(fileSize, result.getFileSize());
        assertEquals(fileContent, result.getFileContent());
        assertEquals(userId, result.getUserId());
        
        verify(fileRepository, times(1)).save(any(File.class));
    }

    @Test
    void getFileById_whenUserOwnsFile_shouldReturnFile() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;
        boolean isAdmin = false;
        
        File file = File.builder()
            .id(fileId)
            .fileName("test.txt")
            .originalFileName("test.txt")
            .contentType("text/plain")
            .fileSize(100L)
            .userId(userId)
            .uploadedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        // When
        File result = fileService.getFileById(fileId, userId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void getFileById_whenAdminAccessesAnyFile_shouldReturnFile() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;
        Long fileOwnerId = 2L;
        boolean isAdmin = true;
        
        File file = File.builder()
            .id(fileId)
            .fileName("test.txt")
            .originalFileName("test.txt")
            .contentType("text/plain")
            .fileSize(100L)
            .userId(fileOwnerId)
            .uploadedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        // When
        File result = fileService.getFileById(fileId, userId, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals(fileOwnerId, result.getUserId());
    }

    @Test
    void getFileById_whenUserDoesNotOwnFile_shouldThrowAccessDeniedException() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;
        Long fileOwnerId = 2L;
        boolean isAdmin = false;
        
        File file = File.builder()
            .id(fileId)
            .fileName("test.txt")
            .originalFileName("test.txt")
            .contentType("text/plain")
            .fileSize(100L)
            .userId(fileOwnerId)
            .uploadedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            fileService.getFileById(fileId, userId, isAdmin);
        });
    }

    @Test
    void getUserFiles_whenAdmin_shouldReturnAllFiles() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        boolean isAdmin = true;
        
        File file = File.builder()
            .id(1L)
            .fileName("test.txt")
            .originalFileName("test.txt")
            .contentType("text/plain")
            .fileSize(100L)
            .userId(2L)
            .uploadedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(fileRepository.findAll(page, size)).thenReturn(List.of(file));

        // When
        List<File> result = fileService.getUserFiles(userId, page, size, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(fileRepository, times(1)).findAll(page, size);
        verify(fileRepository, never()).findByUserId(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getUserFiles_whenRegularUser_shouldReturnUserFiles() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        boolean isAdmin = false;
        
        File file = File.builder()
            .id(1L)
            .fileName("test.txt")
            .originalFileName("test.txt")
            .contentType("text/plain")
            .fileSize(100L)
            .userId(userId)
            .uploadedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(fileRepository.findByUserId(userId, page, size)).thenReturn(List.of(file));

        // When
        List<File> result = fileService.getUserFiles(userId, page, size, isAdmin);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(fileRepository, times(1)).findByUserId(userId, page, size);
        verify(fileRepository, never()).findAll(anyInt(), anyInt());
    }

    @Test
    void deleteFile_shouldCallRepositoryDelete() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;
        boolean isAdmin = false;
        
        File file = File.builder()
            .id(fileId)
            .fileName("test.txt")
            .originalFileName("test.txt")
            .contentType("text/plain")
            .fileSize(100L)
            .userId(userId)
            .uploadedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        // When
        fileService.deleteFile(fileId, userId, isAdmin);

        // Then
        verify(fileRepository, times(1)).deleteById(fileId);
    }
    
    @Test
    void getFileContent_shouldReturnFileContent() throws Exception {
        // Given
        Long fileId = 1L;
        Long userId = 1L;
        boolean isAdmin = false;
        byte[] expectedContent = "test content".getBytes();
        
        File file = File.builder()
            .id(fileId)
            .fileName("test.txt")
            .originalFileName("test.txt")
            .contentType("text/plain")
            .fileSize(100L)
            .fileContent(expectedContent)
            .userId(userId)
            .uploadedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        // When
        byte[] result = fileService.getFileContent(fileId, userId, isAdmin);

        // Then
        assertNotNull(result);
        assertArrayEquals(expectedContent, result);
    }
}
