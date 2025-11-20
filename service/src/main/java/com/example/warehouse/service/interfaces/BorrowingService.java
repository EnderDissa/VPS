package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.enumeration.BorrowStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface BorrowingService {
    Borrowing create(Borrowing entity);
    Borrowing getById(Long id);
    void activate(Long id);
    Borrowing extend(Long id, LocalDateTime newDueAt);
    Borrowing returnBorrowing(Long id);
    void cancel(Long id);
    Page<Borrowing> findPage(int page, int size, BorrowStatus status, Long userId, Long itemId,
                                LocalDateTime from, LocalDateTime to);
    Page<Borrowing> findOverdue(int page, int size);
}
