package com.example.warehouse.dto.UserDTO;

import com.example.warehouse.enumeration.RoleType;

import java.time.LocalDateTime;
import com.example.warehouse.entity.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {

    private Long id;
    private String firstName;
    private String secondName;
    private String lastName;
    private RoleType role;
    private String email;
    private LocalDateTime createdAt;

    public UserResponseDTO(User user) {
        if (user == null) return;
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.secondName = user.getSecondName();
        this.lastName = user.getLastName();
        this.role = user.getRole();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
    }
}
