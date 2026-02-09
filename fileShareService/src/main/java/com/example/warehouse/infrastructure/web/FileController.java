package com.example.warehouse.infrastructure.web;

import com.example.warehouse.application.ports.input.interfaces.FileService;
import com.example.warehouse.infrastructure.auth.UserDetailsEntity;
import com.example.warehouse.infrastructure.auth.UserService;
import com.example.warehouse.infrastructure.web.dto.FileDTO;
import com.example.warehouse.infrastructure.web.mapper.FileMapper;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/files")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;
    private final FileMapper fileMapper;
    private final UserService userService;

    @Autowired
    public FileController(FileService fileService, FileMapper fileMapper, UserService userService) {
        this.fileService = fileService;
        this.fileMapper = fileMapper;
        this.userService = userService;
    }

    @PostMapping(
            path = "/upload",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,  
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<FileDTO>> uploadFile(
            @RequestBody byte[] fileBytes,
            @RequestHeader("X-Filename") String filename,
            @RequestHeader(value = "Content-Type", required = false) String contentType
    ) {
        
        if (fileBytes == null || fileBytes.length == 0) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        
        String finalContentType = contentType != null && !MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(contentType)
                ? contentType
                : URLConnection.guessContentTypeFromName(filename);

        if (finalContentType == null) {
            finalContentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        long fileSize = fileBytes.length;

        
        String finalContentType1 = finalContentType;
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    try {
                        Authentication authentication = securityContext.getAuthentication();
                        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                        Long userId = Long.parseLong(userDetails.getUsername()); 

                        
                        return Mono.fromCallable(() -> {
                                    
                                    com.example.warehouse.domain.model.File uploadedFile = fileService.uploadFile(
                                            filename,           
                                            filename,           
                                            finalContentType1,
                                            fileSize,
                                            fileBytes,          
                                            userId
                                    );
                                    FileDTO fileDTO = fileMapper.toDTO(uploadedFile);
                                    return ResponseEntity.status(HttpStatus.CREATED).body(fileDTO);
                                })
                                .subscribeOn(Schedulers.boundedElastic()) 
                                .onErrorMap(throwable -> {
                                    log.error("Upload failed for user {}: {}", userId, throwable.getMessage(), throwable);
                                    return new RuntimeException("File upload failed", throwable);
                                });
                    } catch (Exception e) {
                        log.error("Security context error: {}", e.getMessage(), e);
                        return Mono.error(e);
                    }
                })
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }
    @GetMapping("/{id}")
    public Mono<ResponseEntity<FileDTO>> getFile(
            @PathVariable Long id) {

        return ReactiveSecurityContextHolder
                .getContext()
                .map(auth -> {
                    UserDetails details = (UserDetails) auth.getAuthentication().getPrincipal();
                    Long userId = Long.parseLong(details.getUsername());

                    boolean isAdmin = isAdminUser(details);
                    com.example.warehouse.domain.model.File file = fileService.getFileById(id, userId, isAdmin);
                    FileDTO fileDTO = fileMapper.toDTO(file);
                    return ResponseEntity.ok(fileDTO);
                });
    }

    @GetMapping
    public Mono<ResponseEntity<List<FileDTO>>> listFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ReactiveSecurityContextHolder
                .getContext()
                .map(auth -> {
                    UserDetails details = (UserDetails) auth.getAuthentication().getPrincipal();
                    Long id = Long.parseLong(details.getUsername());

                    boolean isAdmin = isAdminUser(details);

                    List<com.example.warehouse.domain.model.File> files = fileService.getUserFiles(id, page, size, isAdmin);
                    List<FileDTO> fileDTOs = fileMapper.toDTOList(files);
                    return ResponseEntity.ok(fileDTOs);
                });


    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteFile(
            @PathVariable Long id) {

        return ReactiveSecurityContextHolder
                .getContext()
                .map(auth -> {
                    UserDetails details = (UserDetails) auth.getAuthentication().getPrincipal();
                    Long userId = Long.parseLong(details.getUsername());

                    boolean isAdmin = isAdminUser(details);
                    fileService.deleteFile(id, userId, isAdmin);
                    return ResponseEntity.noContent().build();
                });
    }

    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> getFileCount(@AuthenticationPrincipal UserDetailsEntity userDetails) {

        return ReactiveSecurityContextHolder
                .getContext()
                .map(auth -> {
                    UserDetails details = (UserDetails) auth.getAuthentication().getPrincipal();
                    Long userId = Long.parseLong(details.getUsername());

                    boolean isAdmin = isAdminUser(details);
                    long count = fileService.getFileCount(userId, isAdmin);
                    return ResponseEntity.ok(count);
                });
    }

    @GetMapping("/download/{id}")
    public Mono<ResponseEntity<?>> downloadFile(@PathVariable Long id) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    Long userId = Long.parseLong(userDetails.getUsername());

                    boolean isAdmin = isAdminUser(userDetails);
                    
                    try {
                        com.example.warehouse.domain.model.File file = fileService.getFileById(id, userId, isAdmin);
                        byte[] fileContent = fileService.getFileContent(id, userId, isAdmin);
                        
                        return Mono.just(ResponseEntity.ok()
                                .header("Content-Disposition", "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                                .header("Content-Type", file.getContentType())
                                .body(fileContent));
                    } catch (Exception e) {
                        log.error("Error downloading file: {}", e.getMessage(), e);
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                    }
                })
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    private boolean isAdminUser(UserDetails userDetails) {
        if (userDetails.getAuthorities() != null) {
            for (GrantedAuthority authority : userDetails.getAuthorities()) {
                if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }
}
