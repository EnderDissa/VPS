package com.example.warehouse.service;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import com.example.warehouse.exception.UserAlreadyExistsException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.mapper.UserMapper;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        log.info("Creating new user with email: {}", userRequestDTO.email());
        
        // Проверяем уникальность email
        if (existsByEmail(userRequestDTO.email())) {
            throw new UserAlreadyExistsException("User with email " + userRequestDTO.email() + " already exists");
        }
        
        // Создаем пользователя
        User user = userMapper.toEntity(userRequestDTO);
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return userMapper.toResponseDTO(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        return userMapper.toResponseDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        
        return userMapper.toResponseDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.debug("Fetching all users");
        
        return userRepository.findAll().stream()
            .map(userMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(RoleType role) {
        log.debug("Fetching users by role: {}", role);
        
        return userRepository.findByRole(role).stream()
            .map(userMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> searchUsersByLastName(String lastName) {
        log.debug("Searching users by last name: {}", lastName);
        
        return userRepository.findByLastNameContainingIgnoreCase(lastName).stream()
            .map(userMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        log.info("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        // Проверяем, не занят ли email другим пользователем
        if (!existingUser.getEmail().equals(userRequestDTO.email()) &&
            existsByEmail(userRequestDTO.email())) {
            throw new UserAlreadyExistsException("Email " + userRequestDTO.email() + " is already taken");
        }
        
        // Обновляем данные
        userMapper.updateUserFromDTO(userRequestDTO, existingUser);
        
        User updatedUser = userRepository.save(existingUser);
        log.info("User with ID: {} updated successfully", id);
        
        return userMapper.toResponseDTO(updatedUser);
    }
    
    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("User with ID: {} deleted successfully", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // Дополнительные методы
    
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching users created between {} and {}", start, end);
        
        return userRepository.findUsersCreatedBetween(start, end).stream()
            .map(userMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public long countUsersByRole(RoleType role) {
        log.debug("Counting users with role: {}", role);
        
        return userRepository.findByRole(role).size();
    }
}