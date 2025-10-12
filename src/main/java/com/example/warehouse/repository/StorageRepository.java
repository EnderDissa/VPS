package com.example.warehouse.repository;

import com.example.warehouse.entity.Storage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    Optional<Storage> findByName(String name);

    boolean existsByName(String name);

    Page<Storage> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Storage> findByNameContainingIgnoreCase(String name);

    List<Storage> findByCapacityGreaterThanEqual(Integer capacity);

    List<Storage> findByCapacityLessThanEqual(Integer capacity);

    @Query("SELECT s FROM Storage s WHERE s.capacity BETWEEN :minCapacity AND :maxCapacity")
    List<Storage> findByCapacityBetween(@Param("minCapacity") Integer minCapacity,
                                        @Param("maxCapacity") Integer maxCapacity);

    @Query("SELECT SUM(s.capacity) FROM Storage s")
    Integer getTotalCapacity();

    @Query("SELECT COUNT(s) FROM Storage s WHERE s.capacity >= :minCapacity")
    long countByCapacityGreaterThanEqual(@Param("minCapacity") Integer minCapacity);

    @Query("SELECT s FROM Storage s ORDER BY s.capacity DESC")
    List<Storage> findAllOrderByCapacityDesc();
}