package com.example.warehouse.mapper;

import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toResponseDTO_ok() {
        User u = new User();
        u.setId(7L);
        u.setFirstName("A");
        u.setSecondName("B");
        u.setLastName("C");
        u.setRole(RoleType.MANAGER);
        u.setEmail("a@b.c");

        UserResponseDTO dto = mapper.toResponseDTO(u);
        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.firstName()).isEqualTo("A");
        assertThat(dto.secondName()).isEqualTo("B");
        assertThat(dto.lastName()).isEqualTo("C");
        assertThat(dto.role()).isEqualTo(RoleType.MANAGER);
        assertThat(dto.email()).isEqualTo("a@b.c");
    }
}
