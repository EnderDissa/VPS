package com.example.warehouse.infrastructure.persistence.entity;

import com.example.warehouse.domain.enumeration.RoleType;
import lombok.*;

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
