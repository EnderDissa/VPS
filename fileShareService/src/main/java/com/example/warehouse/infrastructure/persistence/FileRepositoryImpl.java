package com.example.warehouse.infrastructure.persistence;

import com.example.warehouse.application.ports.output.FileRepository;
import com.example.warehouse.domain.model.File;
import com.example.warehouse.infrastructure.persistence.entity.FileEntity;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
public class FileRepositoryImpl implements FileRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public File save(File file) {
        FileEntity entity = toEntity(file);
        if (entity.getId() == null) {
            entity.setUploadedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entityManager.persist(entity);
        } else {
            entity.setUpdatedAt(LocalDateTime.now());
            entity = entityManager.merge(entity);
        }
        entityManager.flush();
        return toDomain(entity);
    }

    @Override
    @Transactional
    public Optional<File> findById(Long id) {
        FileEntity entity = entityManager.find(FileEntity.class, id);
        return entity != null ? Optional.of(toDomain(entity)) : Optional.empty();
    }

    @Override
    @Transactional
    public List<File> findByUserId(Long userId, int page, int size) {
        TypedQuery<FileEntity> query = entityManager.createQuery(
            "SELECT f FROM FileEntity f WHERE f.userId = :userId ORDER BY f.uploadedAt DESC", FileEntity.class);
        query.setParameter("userId", userId);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<File> findAll(int page, int size) {
        TypedQuery<FileEntity> query = entityManager.createQuery(
            "SELECT f FROM FileEntity f ORDER BY f.uploadedAt DESC", FileEntity.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        FileEntity entity = entityManager.find(FileEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    @Transactional
    public long countByUserId(Long userId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(f) FROM FileEntity f WHERE f.userId = :userId", Long.class);
        query.setParameter("userId", userId);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public long countAll() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(f) FROM FileEntity f", Long.class);
        return query.getSingleResult();
    }

    private FileEntity toEntity(File file) {
        return FileEntity.builder()
            .id(file.getId())
            .fileName(file.getFileName())
            .originalFileName(file.getOriginalFileName())
            .contentType(file.getContentType())
            .fileSize(file.getFileSize())
            .userId(file.getUserId())
            .uploadedAt(file.getUploadedAt())
            .updatedAt(file.getUpdatedAt())
            .fileContent(file.getFileContent())
            .build();
    }

    private File toDomain(FileEntity entity) {
        return File.builder()
            .id(entity.getId())
            .fileName(entity.getFileName())
            .originalFileName(entity.getOriginalFileName())
            .contentType(entity.getContentType())
            .fileSize(entity.getFileSize())
            .userId(entity.getUserId())
            .uploadedAt(entity.getUploadedAt())
            .updatedAt(entity.getUpdatedAt())
            .fileContent(entity.getFileContent())
            .build();
    }
}
