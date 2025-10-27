package com.example.warehouse.service;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.exception.UserAlreadyExistsException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.mapper.UserMapper;
import com.example.warehouse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userMapper = mock(UserMapper.class);
        service = new UserServiceImpl(userRepository, userMapper);
    }

    @Test
    void create_ok() {
        UserRequestDTO rq = new UserRequestDTO(
                null,
                "Neo",
                "The",
                "Anderson",
                RoleType.STUDENT,
                "neo@matrix.io",
                LocalDateTime.now()
        );

        when(userRepository.existsByEmail("neo@matrix.io")).thenReturn(false);

        User entity = new User();
        when(userMapper.toEntity(rq)).thenReturn(entity);

        User saved = new User(); saved.setId(1L);
        when(userRepository.save(entity)).thenReturn(saved);

        UserResponseDTO out = new UserResponseDTO(saved);
        when(userMapper.toResponseDTO(saved)).thenReturn(out);

        var rs = service.createUser(rq);
        assertThat(rs.id()).isEqualTo(1L);
    }

    @Test
    void create_duplicateEmail_throws() {
        UserRequestDTO rq = new UserRequestDTO(
                null, "A", "B", "dup@ex.com",
                RoleType.STUDENT, "dup@ex.com", LocalDateTime.now()
        );

        when(userRepository.existsByEmail("dup@ex.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(rq))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @Disabled
    void update_ok() {
        UserRequestDTO rq = new UserRequestDTO(
                null,
                "Trinity",
                "The",
                "X",
                RoleType.DRIVER,
                "t@ex.com",
                LocalDateTime.now()
        );

        User existing = new User(); existing.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));

        doAnswer(inv -> {
            UserRequestDTO dto = inv.getArgument(0);
            User target = inv.getArgument(1);
            target.setFirstName(dto.firstName());
            target.setSecondName(dto.secondName());
            target.setLastName(dto.lastName());
            target.setEmail(dto.email());
            target.setRole(dto.role());
            return null;
        }).when(userMapper).updateUserFromDTO(any(UserRequestDTO.class), any(User.class));

        User saved = new User(); saved.setId(5L);
        when(userRepository.save(existing)).thenReturn(saved);

        UserResponseDTO out = new UserResponseDTO(saved);
        when(userMapper.toResponseDTO(saved)).thenReturn(out);

        var rs = service.updateUser(5L, rq);
        assertThat(rs.id()).isEqualTo(5L);
    }

    @Test
    void getById_notFound() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUserById(42L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
