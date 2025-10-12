package com.example.warehouse.service;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.enumeration.RoleType;
import org.springframework.data.domain.Page;

public interface UserService {
    UserResponseDTO create(UserRequestDTO dto);
    UserResponseDTO getById(Long id);
    void update(Long id, UserRequestDTO dto);
    void delete(Long id);
    Page<UserResponseDTO> findPage(int page, int size, RoleType role, String emailLike);
}
