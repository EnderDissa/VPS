package com.example.warehouse.dto;

import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.enumeration.BorrowStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record BorrowingDTO(
        Long id,

        @NotNull(message = "Item ID is required")
        Long itemId,

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @NotNull(message = "Borrow date is required")
        LocalDateTime borrowDate,

        @NotNull(message = "Expected return date is required")
        @Future(message = "Expected return date must be in the future")
        LocalDateTime expectedReturnDate,

        LocalDateTime actualReturnDate,

        @NotNull(message = "Status is required")
        BorrowStatus status,

        String purpose
) {

    public BorrowingDTO {
        if (quantity == null) {
            quantity = 1;
        }
        if (borrowDate == null) {
            borrowDate = LocalDateTime.now();
        }
        if (status == null) {
            status = BorrowStatus.ACTIVE;
        }
    }

    public BorrowingDTO(Borrowing borrowing) {
        this(
                borrowing != null ? borrowing.getId() : null,
                borrowing != null && borrowing.getItem() != null ? borrowing.getItem().getId() : null,
                borrowing != null && borrowing.getUser() != null ? borrowing.getUser().getId() : null,
                borrowing != null ? borrowing.getQuantity() : 1,
                borrowing != null ? borrowing.getBorrowDate() : LocalDateTime.now(),
                borrowing != null ? borrowing.getExpectedReturnDate() : null,
                borrowing != null ? borrowing.getActualReturnDate() : null,
                borrowing != null ? borrowing.getStatus() : BorrowStatus.ACTIVE,
                borrowing != null ? borrowing.getPurpose() : null
        );
    }
}