package com.example.warehouse.dto;

import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.enumeration.BorrowStatus;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BorrowingDTO {

    public BorrowingDTO(Borrowing borrowing) {
        if (borrowing == null) return;
        this.id = borrowing.getId();
        this.itemId = borrowing.getItem() != null ? borrowing.getItem().getId() : null;
        this.userId = borrowing.getUser() != null ? borrowing.getUser().getId() : null;
        this.quantity = borrowing.getQuantity();
        this.borrowDate = borrowing.getBorrowDate();
        this.expectedReturnDate = borrowing.getExpectedReturnDate();
        this.actualReturnDate = borrowing.getActualReturnDate();
        this.status = borrowing.getStatus();
        this.purpose = borrowing.getPurpose();
    }

    private Long id;

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;

    @NotNull(message = "Borrow date is required")
    private LocalDateTime borrowDate = LocalDateTime.now();

    @NotNull(message = "Expected return date is required")
    @Future(message = "Expected return date must be in the future")
    private LocalDateTime expectedReturnDate;

    private LocalDateTime actualReturnDate;

    @NotNull(message = "Status is required")
    private BorrowStatus status = BorrowStatus.ACTIVE;

    private String purpose;
}