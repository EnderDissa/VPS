package com.example.warehouse.service;

import com.example.warehouse.infrastructure.persistence.entity.User;
import com.example.warehouse.domain.enumeration.RoleType;
import com.example.warehouse.exception.UserAlreadyExistsException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.application.input.UserServiceImpl;
import com.example.warehouse.application.output.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private User testUser4;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .id(1L)
                .firstName("John")
                .secondName("Michael")
                .lastName("Doe")
                .role(RoleType.ADMIN)
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        testUser2 = User.builder()
                .id(2L)
                .firstName("Jane")
                .secondName("Marie")
                .lastName("Smith")
                .role(RoleType.STUDENT)
                .email("jane.smith@example.com")
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        testUser3 = User.builder()
                .id(3L)
                .firstName("Bob")
                .secondName(null)
                .lastName("Johnson")
                .role(RoleType.DRIVER)
                .email("bob.johnson@example.com")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        testUser4 = User.builder()
                .id(4L)
                .firstName("Alice")
                .secondName("Ann")
                .lastName("Williams")
                .role(RoleType.STUDENT)
                .email("alice.williams@example.com")
                .createdAt(LocalDateTime.now().minusHours(12))
                .build();
    }

    @Test
    void createUser_ShouldCreateUser_WhenValidData() {
        User newUser = User.builder()
                .firstName("New")
                .secondName("Middle")
                .lastName("User")
                .role(RoleType.STUDENT)
                .password("1243534")
                .email("new.user@example.com")
                .createdAt(null)
                .build();

        User savedUser = User.builder()
                .id(5L)
                .firstName("New")
                .secondName("Middle")
                .lastName("User")
                .role(RoleType.STUDENT)
                .email("new.user@example.com")
                .password("1243534")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.existsByEmail("new.user@example.com")).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        StepVerifier.create(userService.createUser(newUser))
                .assertNext(user -> {
                    assertNotNull(user.getId());
                    assertEquals("New", user.getFirstName());
                    assertEquals("Middle", user.getSecondName());
                    assertEquals("User", user.getLastName());
                    assertEquals(RoleType.STUDENT, user.getRole());
                    assertEquals("new.user@example.com", user.getEmail());
                    assertNotNull(user.getCreatedAt());
                })
                .verifyComplete();

        verify(userRepository).existsByEmail("new.user@example.com");
        verify(userRepository).save(argThat(user ->
                user.getFirstName().equals("New") &&
                        user.getCreatedAt() != null
        ));
    }

    @Test
    void createUser_ShouldThrowUserAlreadyExistsException_WhenEmailExists() {
        User duplicateUser = User.builder()
                .firstName("Different")
                .secondName("Name")
                .lastName("User")
                .role(RoleType.STUDENT)
                .email("john.doe@example.com")
                .build();

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userService.createUser(duplicateUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().contains("User with email john.doe@example.com already exists")
                )
                .verify();

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser1));

        StepVerifier.create(userService.getUserById(1L))
                .assertNext(user -> {
                    assertEquals(testUser1.getId(), user.getId());
                    assertEquals(testUser1.getFirstName(), user.getFirstName());
                    assertEquals(testUser1.getSecondName(), user.getSecondName());
                    assertEquals(testUser1.getLastName(), user.getLastName());
                    assertEquals(testUser1.getEmail(), user.getEmail());
                    assertEquals(testUser1.getRole(), user.getRole());
                    assertEquals(testUser1.getCreatedAt(), user.getCreatedAt());
                })
                .verifyComplete();

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        Long nonExistentId = 999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserById(nonExistentId))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains("User not found with ID: " + nonExistentId)
                )
                .verify();

        verify(userRepository).findById(nonExistentId);
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Mono.just(testUser1));

        StepVerifier.create(userService.getUserByEmail("john.doe@example.com"))
                .assertNext(user -> {
                    assertEquals(testUser1.getId(), user.getId());
                    assertEquals(testUser1.getEmail(), user.getEmail());
                })
                .verifyComplete();

        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void getUserByEmail_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserByEmail(nonExistentEmail))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains("User not found with email: " + nonExistentEmail)
                )
                .verify();

        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void getAllUsers_ShouldReturnEmptyFlux_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(userService.getAllUsers())
                .expectNextCount(0)
                .verifyComplete();

        verify(userRepository).findAll();
    }

    @Test
    void searchUsersByLastName_ShouldReturnUsers_WhenLastNameMatches() {
        when(userRepository.findByLastNameContainingIgnoreCase("Smith"))
                .thenReturn(Flux.just(testUser2));

        StepVerifier.create(userService.searchUsersByLastName("Smith"))
                .assertNext(user -> {
                    assertEquals("Jane", user.getFirstName());
                    assertEquals("Smith", user.getLastName());
                })
                .verifyComplete();

        verify(userRepository).findByLastNameContainingIgnoreCase("Smith");
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenValidData() {
        User update = User.builder()
                .id(1L)
                .firstName("JohnUpdated")
                .secondName("MichaelUpdated")
                .lastName("DoeUpdated")
                .role(RoleType.MANAGER)
                .email("john.updated@example.com")
                .createdAt(null)
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .firstName("JohnUpdated")
                .secondName("MichaelUpdated")
                .lastName("DoeUpdated")
                .role(RoleType.MANAGER)
                .email("john.updated@example.com")
                .createdAt(testUser1.getCreatedAt())
                .build();

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser1));
        when(userRepository.existsByEmail("john.updated@example.com")).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        StepVerifier.create(userService.updateUser(1L, update))
                .assertNext(user -> {
                    assertEquals("JohnUpdated", user.getFirstName());
                    assertEquals("MichaelUpdated", user.getSecondName());
                    assertEquals("DoeUpdated", user.getLastName());
                    assertEquals(RoleType.MANAGER, user.getRole());
                    assertEquals("john.updated@example.com", user.getEmail());
                })
                .verifyComplete();

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("john.updated@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldNotCheckEmail_WhenEmailNotChanged() {
        User update = User.builder()
                .id(1L)
                .firstName("JohnUpdated")
                .secondName(testUser1.getSecondName())
                .lastName(testUser1.getLastName())
                .role(testUser1.getRole())
                .email(testUser1.getEmail())
                .createdAt(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser1));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(update));

        StepVerifier.create(userService.updateUser(1L, update))
                .assertNext(user -> {
                    assertEquals("JohnUpdated", user.getFirstName());
                    assertEquals(testUser1.getEmail(), user.getEmail());
                })
                .verifyComplete();

        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowUserAlreadyExistsException_WhenEmailTakenByOtherUser() {
        User update = User.builder()
                .id(1L)
                .firstName("JohnUpdated")
                .secondName(testUser1.getSecondName())
                .lastName(testUser1.getLastName())
                .role(testUser1.getRole())
                .email("jane.smith@example.com")
                .createdAt(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser1));
        when(userRepository.existsByEmail("jane.smith@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userService.updateUser(1L, update))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().contains("Email jane.smith@example.com is already taken")
                )
                .verify();

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("jane.smith@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(1L))
                .verifyComplete();

        verify(userRepository).deleteById(1L);
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userService.existsByEmail("john.doe@example.com"))
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).existsByEmail("john.doe@example.com");
    }

    @Test
    void getUsersCreatedBetween_ShouldReturnUsersInDateRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();
        List<User> usersInRange = List.of(testUser2, testUser3);

        when(userRepository.findUsersCreatedBetween(start, end))
                .thenReturn(Flux.fromIterable(usersInRange));

        StepVerifier.create(userService.getUsersCreatedBetween(start, end))
                .expectNextCount(2)
                .verifyComplete();

        verify(userRepository).findUsersCreatedBetween(start, end);
    }

    @Test
    void countUsersByRole_ShouldReturnCorrectCount() {
        when(userRepository.countByRole(RoleType.ADMIN.name())).thenReturn(Mono.just(1L));
        when(userRepository.countByRole(RoleType.STUDENT.name())).thenReturn(Mono.just(2L));

        StepVerifier.create(userService.countUsersByRole(RoleType.ADMIN.name()))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userService.countUsersByRole(RoleType.STUDENT.name()))
                .expectNext(2L)
                .verifyComplete();

        verify(userRepository).countByRole(RoleType.ADMIN.name());
        verify(userRepository).countByRole(RoleType.STUDENT.name());
    }

    @Test
    void updateUser_ShouldPreserveCreatedAt_WhenEmailNotChanged() {
        User update = User.builder()
                .id(1L)
                .firstName("UpdatedName")
                .secondName(testUser1.getSecondName())
                .lastName(testUser1.getLastName())
                .role(testUser1.getRole())
                .email(testUser1.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            return Mono.just(userToSave);
        });

        StepVerifier.create(userService.updateUser(1L, update))
                .assertNext(updated -> {
                    assertEquals(testUser1.getCreatedAt(), updated.getCreatedAt());
                })
                .verifyComplete();

        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(user ->
                user.getCreatedAt().equals(testUser1.getCreatedAt())
        ));
    }

    @Test
    void createUser_ShouldHandleNullSecondName() {
        User newUser = User.builder()
                .firstName("NoMiddle")
                .secondName(null)
                .lastName("Name")
                .role(RoleType.DRIVER)
                .email("nomiddle.name@example.com")
                .password("1243534")
                .createdAt(null)
                .build();

        User savedUser = User.builder()
                .id(6L)
                .firstName("NoMiddle")
                .secondName(null)
                .lastName("Name")
                .role(RoleType.DRIVER)
                .email("nomiddle.name@example.com")
                .createdAt(LocalDateTime.now())
                .password("1243534")
                .build();

        when(userRepository.existsByEmail("nomiddle.name@example.com")).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        StepVerifier.create(userService.createUser(newUser))
                .assertNext(user -> {
                    assertNull(user.getSecondName());
                    assertEquals("NoMiddle", user.getFirstName());
                    assertEquals("Name", user.getLastName());
                })
                .verifyComplete();
    }

    @Test
    void searchUsersByLastName_ShouldReturnEmptyFlux_WhenNoMatches() {
        when(userRepository.findByLastNameContainingIgnoreCase("Nonexistent"))
                .thenReturn(Flux.empty());

        StepVerifier.create(userService.searchUsersByLastName("Nonexistent"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getUsersByRole_ShouldReturnEmptyFlux_WhenNoUsersWithRole() {
        when(userRepository.findByRole(RoleType.MANAGER.name()))
                .thenReturn(Flux.empty());

        StepVerifier.create(userService.getUsersByRole(RoleType.MANAGER.name()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void updateUser_ShouldHandleSecondNameSetToNull() {
        User update = User.builder()
                .id(2L)
                .firstName(testUser2.getFirstName())
                .secondName(null)
                .lastName(testUser2.getLastName())
                .role(testUser2.getRole())
                .email(testUser2.getEmail())
                .createdAt(null)
                .build();

        User updatedUser = User.builder()
                .id(2L)
                .firstName(testUser2.getFirstName())
                .secondName(null)
                .lastName(testUser2.getLastName())
                .role(testUser2.getRole())
                .email(testUser2.getEmail())
                .createdAt(testUser2.getCreatedAt())
                .build();

        when(userRepository.findById(2L)).thenReturn(Mono.just(testUser2));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        StepVerifier.create(userService.updateUser(2L, update))
                .assertNext(user -> {
                    assertNull(user.getSecondName());
                    assertEquals(testUser2.getFirstName(), user.getFirstName());
                })
                .verifyComplete();
    }

    @Test
    void updateUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        Long nonExistentId = 999L;
        User update = User.builder()
                .id(nonExistentId)
                .firstName("Name")
                .secondName("Middle")
                .lastName("Last")
                .role(RoleType.STUDENT)
                .email("email@example.com")
                .createdAt(null)
                .build();

        when(userRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.updateUser(nonExistentId, update))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains("User not found with ID: " + nonExistentId)
                )
                .verify();

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_ShouldCompleteSuccessfully_WhenUserExists() {
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(1L))
                .verifyComplete();

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldCompleteSuccessfully_EvenWhenUserDoesNotExist() {
        Long nonExistentId = 999L;
        when(userRepository.deleteById(nonExistentId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(nonExistentId))
                .verifyComplete();

        verify(userRepository).deleteById(nonExistentId);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(Mono.just(false));

        StepVerifier.create(userService.existsByEmail("nonexistent@example.com"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void getUsersCreatedBetween_ShouldReturnEmptyFlux_WhenNoUsersInDateRange() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        when(userRepository.findUsersCreatedBetween(start, end))
                .thenReturn(Flux.empty());

        StepVerifier.create(userService.getUsersCreatedBetween(start, end))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void searchUsersByLastName_ShouldHandleEmptyString() {
        when(userRepository.findByLastNameContainingIgnoreCase(""))
                .thenReturn(Flux.fromIterable(List.of(testUser1, testUser2, testUser3, testUser4)));

        StepVerifier.create(userService.searchUsersByLastName(""))
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void searchUsersByLastName_ShouldBeCaseInsensitive() {
        when(userRepository.findByLastNameContainingIgnoreCase("smith"))
                .thenReturn(Flux.just(testUser2));

        StepVerifier.create(userService.searchUsersByLastName("smith"))
                .assertNext(user -> {
                    assertEquals("Smith", user.getLastName());
                })
                .verifyComplete();
    }

    @Test
    void createUser_ShouldSetCreatedAtTimestamp() {
        User newUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .role(RoleType.STUDENT)
                .password("1243534")
                .email("test.user@example.com")
                .build();

        when(userRepository.existsByEmail("test.user@example.com")).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            User saved = User.builder()
                    .id(100L)
                    .firstName(userToSave.getFirstName())
                    .lastName(userToSave.getLastName())
                    .role(userToSave.getRole())
                    .email(userToSave.getEmail())
                    .createdAt(userToSave.getCreatedAt())
                    .build();
            return Mono.just(saved);
        });

        StepVerifier.create(userService.createUser(newUser))
                .assertNext(user -> {
                    assertNotNull(user.getCreatedAt());
                })
                .verifyComplete();

        verify(userRepository).save(argThat(user -> user.getCreatedAt() != null));
    }
}