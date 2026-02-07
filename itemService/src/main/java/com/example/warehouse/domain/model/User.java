package com.example.warehouse.domain.model;

import com.example.warehouse.domain.enumeration.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String firstName;
    private String secondName;
    private String lastName;
    private RoleType role;
    private String email;
    private LocalDateTime createdAt;
}
