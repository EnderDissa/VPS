package com.example.warehouse.entity;

import com.example.warehouse.enumeration.BorrowStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "borrowings")
public class Borrowing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Item is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity = 1;

    @NotNull(message = "Borrow date is required")
    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate = LocalDateTime.now();

    @NotNull(message = "Expected return date is required")
    @Column(name = "expected_return_date", nullable = false)
    private LocalDateTime expectedReturnDate;

    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status = BorrowStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String purpose;
}
