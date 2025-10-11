package com.example.warehouse.mapper;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;

public interface UserMapper {
    UserResponseDTO toDTO(User object);
    User toEntity(UserRequestDTO dto);
}
