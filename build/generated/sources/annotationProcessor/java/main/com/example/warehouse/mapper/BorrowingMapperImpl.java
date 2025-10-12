package com.example.warehouse.mapper;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T17:14:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class BorrowingMapperImpl implements BorrowingMapper {

    @Override
    public BorrowingDTO toDTO(Borrowing object) {
        if ( object == null ) {
            return null;
        }

        Borrowing borrowing = null;

        BorrowingDTO borrowingDTO = new BorrowingDTO( borrowing );

        borrowingDTO.setId( object.getId() );
        borrowingDTO.setQuantity( object.getQuantity() );
        borrowingDTO.setBorrowDate( object.getBorrowDate() );
        borrowingDTO.setExpectedReturnDate( object.getExpectedReturnDate() );
        borrowingDTO.setActualReturnDate( object.getActualReturnDate() );
        borrowingDTO.setStatus( object.getStatus() );
        borrowingDTO.setPurpose( object.getPurpose() );

        return borrowingDTO;
    }

    @Override
    public Borrowing toEntity(BorrowingDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Borrowing borrowing = new Borrowing();

        borrowing.setId( dto.getId() );
        borrowing.setQuantity( dto.getQuantity() );
        borrowing.setBorrowDate( dto.getBorrowDate() );
        borrowing.setExpectedReturnDate( dto.getExpectedReturnDate() );
        borrowing.setActualReturnDate( dto.getActualReturnDate() );
        borrowing.setStatus( dto.getStatus() );
        borrowing.setPurpose( dto.getPurpose() );

        return borrowing;
    }
}
