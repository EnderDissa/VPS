package com.example.warehouse.mapper;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    private final ItemMapper mapper = Mappers.getMapper(ItemMapper.class);

    @Test
    void toDTO_maps_basicFields() {
        Item i = new Item();
        i.setId(1L);
        i.setSerialNumber("SN-1");
        i.setName("Hammer");
        i.setType(ItemType.TOOLS);
        i.setCondition(ItemCondition.NEW);

        ItemDTO dto = mapper.toDTO(i);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.serialNumber()).isEqualTo("SN-1");
        assertThat(dto.name()).isEqualTo("Hammer");
        assertThat(dto.type()).isEqualTo(ItemType.TOOLS);
        assertThat(dto.condition()).isEqualTo(ItemCondition.NEW);
    }
}
