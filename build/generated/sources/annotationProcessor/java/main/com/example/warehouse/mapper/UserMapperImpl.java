package com.example.warehouse.mapper;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;
import com.example.warehouse.enumeration.RoleType;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T16:49:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.0.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDTO toResponseDTO(User user) {
        if ( user == null ) {
            return null;
        }

        Long id = null;
        String firstName = null;
        String secondName = null;
        String lastName = null;
        RoleType role = null;
        String email = null;
        LocalDateTime createdAt = null;

        id = user.getId();
        firstName = user.getFirstName();
        secondName = user.getSecondName();
        lastName = user.getLastName();
        role = user.getRole();
        email = user.getEmail();
        createdAt = user.getCreatedAt();

        UserResponseDTO userResponseDTO = new UserResponseDTO( id, firstName, secondName, lastName, role, email, createdAt );

        return userResponseDTO;
    }

    @Override
    public User toEntity(UserRequestDTO userRequestDTO) {
        if ( userRequestDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.firstName( userRequestDTO.firstName() );
        user.secondName( userRequestDTO.secondName() );
        user.lastName( userRequestDTO.lastName() );
        user.role( userRequestDTO.role() );
        user.email( userRequestDTO.email() );

        return user.build();
    }

    @Override
    public void updateUserFromDTO(UserRequestDTO userRequestDTO, User user) {
        if ( userRequestDTO == null ) {
            return;
        }

        user.setFirstName( userRequestDTO.firstName() );
        user.setSecondName( userRequestDTO.secondName() );
        user.setLastName( userRequestDTO.lastName() );
        user.setRole( userRequestDTO.role() );
        user.setEmail( userRequestDTO.email() );
    }
}
