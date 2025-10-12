package com.example.warehouse.dto.UserDTO;

import com.example.warehouse.enumeration.RoleType;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UserRequestDTO {

    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String secondName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotNull(message = "Role is required")
    private RoleType role;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private LocalDateTime createdAt;
}