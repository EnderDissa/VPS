package com.example.warehouse.mapper;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.entity.Item;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T17:14:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class ItemMapperImpl implements ItemMapper {

    @Override
    public ItemDTO toDTO(Item object) {
        if ( object == null ) {
            return null;
        }

        Item item = null;

        ItemDTO itemDTO = new ItemDTO( item );

        itemDTO.setId( object.getId() );
        itemDTO.setName( object.getName() );
        itemDTO.setType( object.getType() );
        itemDTO.setCondition( object.getCondition() );
        itemDTO.setSerialNumber( object.getSerialNumber() );
        itemDTO.setDescription( object.getDescription() );
        itemDTO.setCreatedAt( object.getCreatedAt() );

        return itemDTO;
    }

    @Override
    public Item toEntity(ItemDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Item item = new Item();

        item.setId( dto.getId() );
        item.setName( dto.getName() );
        item.setType( dto.getType() );
        item.setCondition( dto.getCondition() );
        item.setSerialNumber( dto.getSerialNumber() );
        item.setDescription( dto.getDescription() );
        item.setCreatedAt( dto.getCreatedAt() );

        return item;
    }
}
