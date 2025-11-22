package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.enumeration.BorrowStatus;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface BorrowingService {
    Mono<Borrowing> getById(Long id);
    Mono<Borrowing> create(BorrowingDTO entity);
    Mono<Void> activate(Long id);
    Mono<Borrowing> extend(Long id, LocalDateTime newDueAt);
    Mono<Borrowing> returnBorrowing(Long id);
    Mono<Void> cancel(Long id);
    Flux<Borrowing> findBorrowingsByFilters(BorrowStatus status, Long userId, Long itemId,
                                            LocalDateTime from, LocalDateTime to, Pageable pageable);
    Mono<Long> countBorrowingsByFilters(BorrowStatus status, Long userId, Long itemId,
                                        LocalDateTime from, LocalDateTime to);
    Flux<Borrowing> findOverdueBorrowings(Pageable pageable);
    Mono<Long> countOverdueBorrowings();
}