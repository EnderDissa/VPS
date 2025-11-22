package com.example.warehouse.mapper;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.BorrowStatus;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T16:49:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.0.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class BorrowingMapperImpl implements BorrowingMapper {

    @Override
    public BorrowingDTO toDTO(Borrowing borrowing) {
        if ( borrowing == null ) {
            return null;
        }

        Long userId = null;
        Long itemId = null;
        Long id = null;
        Integer quantity = null;
        LocalDateTime borrowDate = null;
        LocalDateTime expectedReturnDate = null;
        LocalDateTime actualReturnDate = null;
        BorrowStatus status = null;
        String purpose = null;

        userId = borrowingUserId( borrowing );
        itemId = borrowingItemId( borrowing );
        id = borrowing.getId();
        quantity = borrowing.getQuantity();
        borrowDate = borrowing.getBorrowDate();
        expectedReturnDate = borrowing.getExpectedReturnDate();
        actualReturnDate = borrowing.getActualReturnDate();
        status = borrowing.getStatus();
        purpose = borrowing.getPurpose();

        BorrowingDTO borrowingDTO = new BorrowingDTO( id, itemId, userId, quantity, borrowDate, expectedReturnDate, actualReturnDate, status, purpose );

        return borrowingDTO;
    }

    @Override
    public Borrowing toEntity(BorrowingDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Borrowing.BorrowingBuilder borrowing = Borrowing.builder();

        borrowing.id( dto.id() );
        borrowing.quantity( dto.quantity() );
        borrowing.borrowDate( dto.borrowDate() );
        borrowing.expectedReturnDate( dto.expectedReturnDate() );
        borrowing.actualReturnDate( dto.actualReturnDate() );
        borrowing.status( dto.status() );
        borrowing.purpose( dto.purpose() );

        return borrowing.build();
    }

    private Long borrowingUserId(Borrowing borrowing) {
        User user = borrowing.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }

    private Long borrowingItemId(Borrowing borrowing) {
        Item item = borrowing.getItem();
        if ( item == null ) {
            return null;
        }
        return item.getId();
    }
}
