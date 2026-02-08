package com.example.warehouse.application.ports.output;

import com.example.warehouse.domain.model.File;
import java.util.List;
import java.util.Optional;

public interface FileRepository {
    File save(File file);
    Optional<File> findById(Long id);
    List<File> findByUserId(Long userId, int page, int size);
    List<File> findAll(int page, int size);
    void deleteById(Long id);
    long countByUserId(Long userId);
    long countAll();
}
