package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;

import java.time.LocalDateTime;
import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO userRequestDTO);
    User getUserById(Long id);
    UserResponseDTO getUserByEmail(String email);
    List<UserResponseDTO> getAllUsers();
    List<UserResponseDTO> getUsersByRole(RoleType role);
    List<UserResponseDTO> searchUsersByLastName(String lastName);
    UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO);
    void deleteUser(Long id);
    boolean existsByEmail(String email);
}
