package com.example.warehouse.repository;

import com.example.warehouse.entity.Borrowing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long>, JpaSpecificationExecutor<Borrowing> {

    @Query("SELECT b FROM Borrowing b WHERE b.status = 'ACTIVE' AND b.expectedReturnDate < :now")
    Page<Borrowing> findOverdueBorrowings(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.status IN ('ACTIVE', 'OVERDUE')")
    long countActiveBorrowingsByUser(@Param("userId") Long userId);

    // Add a method to count overdue borrowings
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.status = 'ACTIVE' AND b.expectedReturnDate < :now")
    long countOverdueBorrowings(@Param("now") LocalDateTime now);

    // Add a method to find borrowings with filters using Specification and Pageable
    Page<Borrowing> findAll(Specification<Borrowing> spec, Pageable pageable);

    // Add a method to count borrowings with filters using Specification
    long count(Specification<Borrowing> spec);
}