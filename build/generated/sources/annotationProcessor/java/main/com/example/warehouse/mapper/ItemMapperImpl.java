package com.example.warehouse.mapper;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T16:49:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.0.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class ItemMapperImpl implements ItemMapper {

    @Override
    public ItemDTO toDTO(Item object) {
        if ( object == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        ItemType type = null;
        ItemCondition condition = null;
        String serialNumber = null;
        String description = null;
        LocalDateTime createdAt = null;

        id = object.getId();
        name = object.getName();
        type = object.getType();
        condition = object.getCondition();
        serialNumber = object.getSerialNumber();
        description = object.getDescription();
        createdAt = object.getCreatedAt();

        ItemDTO itemDTO = new ItemDTO( id, name, type, condition, serialNumber, description, createdAt );

        return itemDTO;
    }

    @Override
    public Item toEntity(ItemDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Item.ItemBuilder item = Item.builder();

        item.id( dto.id() );
        item.name( dto.name() );
        item.type( dto.type() );
        item.condition( dto.condition() );
        item.serialNumber( dto.serialNumber() );
        item.description( dto.description() );
        item.createdAt( dto.createdAt() );

        return item.build();
    }
}
