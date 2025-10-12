package com.example.warehouse.service;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.enumeration.BorrowStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface BorrowingService {
    BorrowingDTO create(BorrowingDTO dto);
    BorrowingDTO getById(Long id);
    void activate(Long id);
    BorrowingDTO extend(Long id, LocalDateTime newDueAt);
    BorrowingDTO returnBorrowing(Long id);
    void cancel(Long id);
    Page<BorrowingDTO> findPage(int page, int size, BorrowStatus status, Long userId, Long itemId,
                                LocalDateTime from, LocalDateTime to);
    Page<BorrowingDTO> findOverdue(int page, int size);
}
