package com.example.warehouse.mapper;

import com.example.warehouse.dto.UserStorageAccessDTO;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.entity.User;
import com.example.warehouse.entity.UserStorageAccess;
import com.example.warehouse.enumeration.AccessLevel;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T16:49:19+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.2.0.jar, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class UserStorageAccessMapperImpl implements UserStorageAccessMapper {

    @Override
    public UserStorageAccessDTO toDTO(UserStorageAccess object) {
        if ( object == null ) {
            return null;
        }

        Long userId = null;
        Long storageId = null;
        Long grantedById = null;
        Long id = null;
        AccessLevel accessLevel = null;
        LocalDateTime grantedAt = null;
        LocalDateTime expiresAt = null;
        Boolean isActive = null;

        userId = objectUserId( object );
        storageId = objectStorageId( object );
        grantedById = objectGrantedById( object );
        id = object.getId();
        accessLevel = object.getAccessLevel();
        grantedAt = object.getGrantedAt();
        expiresAt = object.getExpiresAt();
        isActive = object.getIsActive();

        UserStorageAccessDTO userStorageAccessDTO = new UserStorageAccessDTO( id, userId, storageId, accessLevel, grantedById, grantedAt, expiresAt, isActive );

        return userStorageAccessDTO;
    }

    @Override
    public UserStorageAccess toEntity(UserStorageAccessDTO dto) {
        if ( dto == null ) {
            return null;
        }

        UserStorageAccess.UserStorageAccessBuilder userStorageAccess = UserStorageAccess.builder();

        userStorageAccess.user( userStorageAccessDTOToUser( dto ) );
        userStorageAccess.storage( userStorageAccessDTOToStorage( dto ) );
        userStorageAccess.grantedBy( userStorageAccessDTOToUser1( dto ) );
        userStorageAccess.id( dto.id() );
        userStorageAccess.accessLevel( dto.accessLevel() );
        userStorageAccess.grantedAt( dto.grantedAt() );
        userStorageAccess.expiresAt( dto.expiresAt() );
        userStorageAccess.isActive( dto.isActive() );

        return userStorageAccess.build();
    }

    private Long objectUserId(UserStorageAccess userStorageAccess) {
        User user = userStorageAccess.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }

    private Long objectStorageId(UserStorageAccess userStorageAccess) {
        Storage storage = userStorageAccess.getStorage();
        if ( storage == null ) {
            return null;
        }
        return storage.getId();
    }

    private Long objectGrantedById(UserStorageAccess userStorageAccess) {
        User grantedBy = userStorageAccess.getGrantedBy();
        if ( grantedBy == null ) {
            return null;
        }
        return grantedBy.getId();
    }

    protected User userStorageAccessDTOToUser(UserStorageAccessDTO userStorageAccessDTO) {
        if ( userStorageAccessDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userStorageAccessDTO.userId() );

        return user.build();
    }

    protected Storage userStorageAccessDTOToStorage(UserStorageAccessDTO userStorageAccessDTO) {
        if ( userStorageAccessDTO == null ) {
            return null;
        }

        Storage.StorageBuilder storage = Storage.builder();

        storage.id( userStorageAccessDTO.storageId() );

        return storage.build();
    }

    protected User userStorageAccessDTOToUser1(UserStorageAccessDTO userStorageAccessDTO) {
        if ( userStorageAccessDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userStorageAccessDTO.grantedById() );

        return user.build();
    }
}
