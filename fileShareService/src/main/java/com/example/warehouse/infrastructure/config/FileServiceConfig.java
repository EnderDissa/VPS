package com.example.warehouse.infrastructure.config;

import com.example.warehouse.application.ports.output.FileRepository;
import com.example.warehouse.infrastructure.persistence.FileRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileServiceConfig {

    @Bean
    public FileRepository fileRepository() {
        return new FileRepositoryImpl();
    }
}
