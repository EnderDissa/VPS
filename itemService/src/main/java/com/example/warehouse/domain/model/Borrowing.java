package com.example.warehouse.domain.model;

import com.example.warehouse.domain.enumeration.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Borrowing {

    private Long id;
    private Item item;
    private Long userId;
    private Integer quantity = 1;
    private LocalDateTime borrowDate = LocalDateTime.now();
    private LocalDateTime expectedReturnDate;
    private LocalDateTime actualReturnDate;
    private BorrowStatus status = BorrowStatus.ACTIVE;
    private String purpose;
}
