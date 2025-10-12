package com.example.warehouse.mapper;

import com.example.warehouse.dto.UserDTO.UserRequestDTO;
import com.example.warehouse.dto.UserDTO.UserResponseDTO;
import com.example.warehouse.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T17:14:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDTO toResponseDTO(User user) {
        if ( user == null ) {
            return null;
        }

        User user1 = null;

        UserResponseDTO userResponseDTO = new UserResponseDTO( user1 );

        userResponseDTO.setId( user.getId() );
        userResponseDTO.setFirstName( user.getFirstName() );
        userResponseDTO.setSecondName( user.getSecondName() );
        userResponseDTO.setLastName( user.getLastName() );
        userResponseDTO.setRole( user.getRole() );
        userResponseDTO.setEmail( user.getEmail() );
        userResponseDTO.setCreatedAt( user.getCreatedAt() );

        return userResponseDTO;
    }

    @Override
    public User toEntity(UserRequestDTO userRequestDTO) {
        if ( userRequestDTO == null ) {
            return null;
        }

        User user = new User();

        user.setFirstName( userRequestDTO.getFirstName() );
        user.setSecondName( userRequestDTO.getSecondName() );
        user.setLastName( userRequestDTO.getLastName() );
        user.setRole( userRequestDTO.getRole() );
        user.setEmail( userRequestDTO.getEmail() );

        return user;
    }

    @Override
    public void updateUserFromDTO(UserRequestDTO userRequestDTO, User user) {
        if ( userRequestDTO == null ) {
            return;
        }

        user.setFirstName( userRequestDTO.getFirstName() );
        user.setSecondName( userRequestDTO.getSecondName() );
        user.setLastName( userRequestDTO.getLastName() );
        user.setRole( userRequestDTO.getRole() );
        user.setEmail( userRequestDTO.getEmail() );
    }
}
