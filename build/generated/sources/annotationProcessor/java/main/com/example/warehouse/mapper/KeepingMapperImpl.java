package com.example.warehouse.mapper;

import com.example.warehouse.dto.KeepingDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.Keeping;
import com.example.warehouse.entity.Storage;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T16:49:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.0.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class KeepingMapperImpl implements KeepingMapper {

    @Override
    public KeepingDTO toDTO(Keeping object) {
        if ( object == null ) {
            return null;
        }

        Long storageId = null;
        Long itemId = null;
        Long id = null;
        Integer quantity = null;
        String shelf = null;
        LocalDateTime lastUpdated = null;

        storageId = objectStorageId( object );
        itemId = objectItemId( object );
        id = object.getId();
        quantity = object.getQuantity();
        shelf = object.getShelf();
        lastUpdated = object.getLastUpdated();

        KeepingDTO keepingDTO = new KeepingDTO( id, storageId, itemId, quantity, shelf, lastUpdated );

        return keepingDTO;
    }

    @Override
    public Keeping toEntity(KeepingDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Keeping.KeepingBuilder keeping = Keeping.builder();

        keeping.storage( keepingDTOToStorage( dto ) );
        keeping.item( keepingDTOToItem( dto ) );
        keeping.id( dto.id() );
        keeping.quantity( dto.quantity() );
        keeping.shelf( dto.shelf() );
        keeping.lastUpdated( dto.lastUpdated() );

        return keeping.build();
    }

    private Long objectStorageId(Keeping keeping) {
        Storage storage = keeping.getStorage();
        if ( storage == null ) {
            return null;
        }
        return storage.getId();
    }

    private Long objectItemId(Keeping keeping) {
        Item item = keeping.getItem();
        if ( item == null ) {
            return null;
        }
        return item.getId();
    }

    protected Storage keepingDTOToStorage(KeepingDTO keepingDTO) {
        if ( keepingDTO == null ) {
            return null;
        }

        Storage.StorageBuilder storage = Storage.builder();

        storage.id( keepingDTO.storageId() );

        return storage.build();
    }

    protected Item keepingDTOToItem(KeepingDTO keepingDTO) {
        if ( keepingDTO == null ) {
            return null;
        }

        Item.ItemBuilder item = Item.builder();

        item.id( keepingDTO.itemId() );

        return item.build();
    }
}
