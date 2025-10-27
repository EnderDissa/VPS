package com.example.warehouse.service;

import com.example.warehouse.dto.BorrowingDTO;
import com.example.warehouse.entity.Borrowing;
import com.example.warehouse.mapper.BorrowingMapper;
import com.example.warehouse.repository.BorrowingRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;

class BorrowingServiceImplTest {

    private BorrowingRepository borrowingRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BorrowingMapper mapper;
    private BorrowingServiceImpl service;

    @BeforeEach
    void setUp() {
        borrowingRepository = mock(BorrowingRepository.class);
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        mapper = mock(BorrowingMapper.class);
        service = new BorrowingServiceImpl(borrowingRepository, itemRepository, userRepository, mapper);
    }

    @Test
    void getById_ok() {
        Borrowing b = new Borrowing(); b.setId(1L);
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(b));
        when(mapper.toDTO(b)).thenReturn(new BorrowingDTO(b));

        var rs = service.getById(1L);
        assertThat(rs).isNotNull();
    }

    @Test
    void getById_notFound() {
        when(borrowingRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(404L)).isInstanceOf(EntityNotFoundException.class);
    }
}
