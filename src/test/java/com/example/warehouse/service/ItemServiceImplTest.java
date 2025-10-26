package com.example.warehouse.service;

import com.example.warehouse.dto.ItemDTO;
import com.example.warehouse.entity.Item;
import com.example.warehouse.enumeration.ItemCondition;
import com.example.warehouse.enumeration.ItemType;
import com.example.warehouse.exception.DuplicateSerialNumberException;
import com.example.warehouse.mapper.ItemMapper;
import com.example.warehouse.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    private ItemRepository itemRepository;
    private ItemMapper itemMapper;
    private ItemServiceImpl service;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        itemMapper = mock(ItemMapper.class);
        service = new ItemServiceImpl(itemRepository, itemMapper);
    }

    @Test
    void create_success() {
        ItemDTO rq = new ItemDTO(
                null,
                "Hammer",
                ItemType.TOOLS,
                ItemCondition.NEW,
                "SN-1",
                null,
                null
        );

        Item toSave = new Item();
        Item saved = new Item(); saved.setId(10L);

        when(itemRepository.existsBySerialNumber("SN-1")).thenReturn(false);
        when(itemMapper.toEntity(rq)).thenReturn(toSave);
        when(itemRepository.save(toSave)).thenReturn(saved);

        ItemDTO out = new ItemDTO(
                10L,
                "Hammer",
                ItemType.TOOLS,
                ItemCondition.NEW,
                "SN-1",
                null,
                null
        );

        when(itemMapper.toDTO(saved)).thenReturn(out);

        ItemDTO rs = service.create(rq);

        assertThat(rs.id()).isEqualTo(10L);
        verify(itemRepository).existsBySerialNumber("SN-1");
        verify(itemRepository).save(toSave);
    }

    @Test
    void update_duplicateSerial_throws() {
        Item existing = new Item();
        existing.setId(7L);
        existing.setSerialNumber("OLD");

        ItemDTO rq = new ItemDTO(
                null,
                "NewName",
                ItemType.TOOLS,
                ItemCondition.NEW,
                "DUP",
                null,
                null
        );

        when(itemRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(itemRepository.existsBySerialNumber("DUP")).thenReturn(true);

        assertThatThrownBy(() -> service.update(7L, rq))
                .isInstanceOf(DuplicateSerialNumberException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void findPage_byTypeAndCondition() {
        var list = List.of(new Item(), new Item());
        Pageable p = PageRequest.of(0, 10);
        when(itemRepository.findByTypeAndCondition(ItemType.TOOLS, ItemCondition.NEW, p))
                .thenReturn(list);
        when(itemMapper.toDTO(any(Item.class))).thenAnswer(inv -> {
            Item arg = inv.getArgument(0);
            return new ItemDTO(
                    arg.getId(),
                    arg.getName(),
                    arg.getType(),
                    arg.getCondition(),
                    arg.getSerialNumber(),
                    arg.getDescription(),
                    arg.getCreatedAt()
            );
        });

        var page = service.findPage(0, 10, ItemType.TOOLS, ItemCondition.NEW);

        assertThat(page.getTotalElements()).isEqualTo(2);
        verify(itemRepository).findByTypeAndCondition(ItemType.TOOLS, ItemCondition.NEW, p);
    }
}
