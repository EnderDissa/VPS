package com.example.warehouse.entity;

import com.example.warehouse.enumeration.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column("first_name")
    private String firstName;

    @Size(max = 100, message = "Second name must not exceed 100 characters")
    @Column("second_name")
    private String secondName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column("last_name")
    private String lastName;

    @NotNull(message = "Role is required")
    @Column("role")
    private RoleType role;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column("email")
    private String email;

    @Column("created_at")
    private LocalDateTime createdAt;
}