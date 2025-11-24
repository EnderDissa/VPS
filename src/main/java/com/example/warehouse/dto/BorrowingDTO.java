package com.example.warehouse.dto;

import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.enumeration.BorrowStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@JsonNaming(value = com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BorrowingDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,

        @NotNull(message = "Item ID is required")
        Long itemId,

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @NotNull(message = "Borrow date is required")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
        LocalDateTime borrowDate,

        @NotNull(message = "Expected return date is required")
        @Future(message = "Expected return date must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
        LocalDateTime expectedReturnDate,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2025-11-24 03:29:34")
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