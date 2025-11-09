package com.example.warehouse.service.interfaces;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;

import java.time.LocalDateTime;
import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    User getUserByEmail(String email);
    List<User> getAllUsers();
    List<User> getUsersByRole(RoleType role);
    List<User> searchUsersByLastName(String lastName);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    boolean existsByEmail(String email);
}
