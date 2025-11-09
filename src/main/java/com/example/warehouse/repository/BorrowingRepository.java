package com.example.warehouse.repository;

import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.enumeration.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long>, JpaSpecificationExecutor<Borrowing> {

    @Query("SELECT b FROM Borrowing b WHERE b.status = 'ACTIVE' AND b.expectedReturnDate < :now")
    Page<Borrowing> findOverdueBorrowings(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.status IN ('ACTIVE', 'OVERDUE')")
    long countActiveBorrowingsByUser(@Param("userId") Long userId);
}