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
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    public User createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        if (existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists");
        }

        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return savedUser;
    }
    
    @Override
    public User getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);

        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Fetching all users");

        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByRole(RoleType role) {
        log.debug("Fetching users by role: {}", role);

        return userRepository.findByRole(role);
    }

    @Override
    public List<User> searchUsersByLastName(String lastName) {
        log.debug("Searching users by last name: {}", lastName);

        return userRepository.findByLastNameContainingIgnoreCase(lastName);
    }
    
    @Override
    public User updateUser(Long id, User user) {
        log.info("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!existingUser.getEmail().equals(user.getEmail()) &&
            existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email " + user.getEmail() + " is already taken");
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User with ID: {} updated successfully", id);
        
        return updatedUser;
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
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> getUsersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching users created between {} and {}", start, end);
        
        return userRepository.findUsersCreatedBetween(start, end);
    }

    public long countUsersByRole(RoleType role) {
        log.debug("Counting users with role: {}", role);
        
        return userRepository.findByRole(role).size();
    }
}