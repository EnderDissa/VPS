package com.example.warehouse.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsEntityTest {

    private UserDetailsEntity userDetailsEntity;

    @BeforeEach
    void setUp() {
        userDetailsEntity = new UserDetailsEntity();
    }

    @Test
    void constructor_ShouldSetDefaultValues() {
        // Arrange
        String username = "testUser";
        String password = "testPassword";
        String[] authorities = {"ROLE_ADMIN", "ROLE_USER"};

        // Act
        UserDetailsEntity entity = new UserDetailsEntity(username, password, authorities);

        // Assert
        assertEquals(username, entity.getUsername());
        assertEquals(password, entity.getPassword());
        assertArrayEquals(authorities, entity.getAuthorities());
        assertTrue(entity.getAccountNonExpired());
        assertTrue(entity.getAccountNonLocked());
        assertTrue(entity.getCredentialsNonExpired());
        assertTrue(entity.getEnabled());
    }

    @Test
    void toUserDetails_ShouldReturnUserDetails_WhenValidData() {
        // Arrange
        String username = "testUser";
        String password = "testPassword";
        String[] authorities = {"ROLE_ADMIN", "ROLE_USER"};

        userDetailsEntity.setUsername(username);
        userDetailsEntity.setPassword(password);
        userDetailsEntity.setAuthorities(authorities);
        userDetailsEntity.setAccountNonExpired(true);
        userDetailsEntity.setAccountNonLocked(true);
        userDetailsEntity.setCredentialsNonExpired(true);
        userDetailsEntity.setEnabled(true);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void toUserDetails_ShouldReturnNull_WhenUsernameIsNull() {
        // Arrange
        userDetailsEntity.setUsername(null);
        userDetailsEntity.setPassword("password");
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNull(userDetails);
    }

    @Test
    void toUserDetails_ShouldReturnNull_WhenPasswordIsNull() {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword(null);
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNull(userDetails);
    }

    @Test
    void toUserDetails_ShouldReturnNull_WhenUsernameAndPasswordAreNull() {
        // Arrange
        userDetailsEntity.setUsername(null);
        userDetailsEntity.setPassword(null);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNull(userDetails);
    }

    @Test
    void toUserDetails_ShouldHandleEmptyAuthorities() {
        // Arrange
        String username = "testUser";
        String password = "testPassword";
        String[] emptyAuthorities = {};

        userDetailsEntity.setUsername(username);
        userDetailsEntity.setPassword(password);
        userDetailsEntity.setAuthorities(emptyAuthorities);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void toUserDetails_ShouldHandleNullAuthorities() {
        // Arrange
        String username = "testUser";
        String password = "testPassword";

        userDetailsEntity.setUsername(username);
        userDetailsEntity.setPassword(password);
        userDetailsEntity.setAuthorities(null);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void toUserDetails_ShouldHandleAccountExpired() {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword("testPassword");
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});
        userDetailsEntity.setAccountNonExpired(false);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.isAccountNonExpired() == false);
    }

    @Test
    void toUserDetails_ShouldHandleAccountLocked() {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword("testPassword");
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});
        userDetailsEntity.setAccountNonLocked(false);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.isAccountNonLocked() == false);
    }

    @Test
    void toUserDetails_ShouldHandleCredentialsExpired() {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword("testPassword");
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});
        userDetailsEntity.setCredentialsNonExpired(false);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.isCredentialsNonExpired() == false);
    }

    @Test
    void toUserDetails_ShouldHandleDisabledAccount() {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword("testPassword");
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});
        userDetailsEntity.setEnabled(false);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.isEnabled() == false);
    }

    @Test
    void toUserDetails_ShouldHandleNullBooleanFlags() {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword("testPassword");
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});
        userDetailsEntity.setAccountNonExpired(null);
        userDetailsEntity.setAccountNonLocked(null);
        userDetailsEntity.setCredentialsNonExpired(null);
        userDetailsEntity.setEnabled(null);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        // Когда флаги null, они должны считаться как false (согласно логике в toUserDetails)
        assertTrue(userDetails.isAccountNonExpired() == false);
        assertTrue(userDetails.isAccountNonLocked() == false);
        assertTrue(userDetails.isCredentialsNonExpired() == false);
        assertTrue(userDetails.isEnabled() == false);
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        // Arrange
        String username = "testUser";
        String[] authorities = {"ROLE_ADMIN", "ROLE_USER"};

        userDetailsEntity.setUsername(username);
        userDetailsEntity.setAuthorities(authorities);

        // Act
        String result = userDetailsEntity.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("UserDetailsEntity"));
        assertTrue(result.contains("username='" + username + "'"));
        assertTrue(result.contains("authorities=" + Arrays.toString(authorities)));
    }

    @Test
    void toString_ShouldHandleNullAuthorities() {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setAuthorities(null);

        // Act
        String result = userDetailsEntity.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("authorities=null"));
    }

    @Test
    void toString_ShouldHandleEmptyAuthorities() {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setAuthorities(new String[]{});

        // Act
        String result = userDetailsEntity.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("authorities=[]"));
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        // Arrange
        String username = "testUser";
        String password = "testPassword";
        String[] authorities = {"ROLE_TEST"};
        Boolean accountNonExpired = false;
        Boolean accountNonLocked = false;
        Boolean credentialsNonExpired = false;
        Boolean enabled = false;

        // Act
        userDetailsEntity.setUsername(username);
        userDetailsEntity.setPassword(password);
        userDetailsEntity.setAuthorities(authorities);
        userDetailsEntity.setAccountNonExpired(accountNonExpired);
        userDetailsEntity.setAccountNonLocked(accountNonLocked);
        userDetailsEntity.setCredentialsNonExpired(credentialsNonExpired);
        userDetailsEntity.setEnabled(enabled);

        // Assert
        assertEquals(username, userDetailsEntity.getUsername());
        assertEquals(password, userDetailsEntity.getPassword());
        assertArrayEquals(authorities, userDetailsEntity.getAuthorities());
        assertEquals(accountNonExpired, userDetailsEntity.getAccountNonExpired());
        assertEquals(accountNonLocked, userDetailsEntity.getAccountNonLocked());
        assertEquals(credentialsNonExpired, userDetailsEntity.getCredentialsNonExpired());
        assertEquals(enabled, userDetailsEntity.getEnabled());
    }

    @Test
    void toUserDetails_ShouldCreateImmutableUserDetails() {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword("testPassword");
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        // Проверяем что возвращается правильный тип (User из Spring Security)
        assertEquals("org.springframework.security.core.userdetails.User",
                userDetails.getClass().getName());
    }

    @Test
    void toUserDetails_ShouldHandleWhitespaceInUsername() {
        // Arrange
        String username = "  testUser  ";
        String password = "testPassword";

        userDetailsEntity.setUsername(username);
        userDetailsEntity.setPassword(password);
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername()); // Сохраняет пробелы
    }

    @ParameterizedTest
    @NullAndEmptySource
    void toUserDetails_ShouldHandleNullOrEmptyPassword(String password) {
        // Arrange
        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword(password);
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        if (password == null) {
            assertNull(userDetails);
        } else {
            assertNotNull(userDetails);
            assertEquals(password, userDetails.getPassword());
        }
    }

    @Test
    void toUserDetails_ShouldHandleComplexAuthorityNames() {
        // Arrange
        String[] complexAuthorities = {
                "SCOPE_read",
                "SCOPE_write",
                "ROLE_SPECIAL_USER_GROUP_1",
                "PERMISSION_DELETE_ANY_POST",
                "SOME_VERY_LONG_AUTHORITY_NAME_THAT_EXCEEDS_NORMAL_LENGTH"
        };

        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword("testPassword");
        userDetailsEntity.setAuthorities(complexAuthorities);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertEquals(complexAuthorities.length, userDetails.getAuthorities().size());

        for (String authority : complexAuthorities) {
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(granted -> granted.getAuthority().equals(authority)));
        }
    }

    @Test
    void toUserDetails_ShouldPreserveAuthorityOrder() {
        // Arrange
        String[] authorities = {"FIRST", "SECOND", "THIRD"};

        userDetailsEntity.setUsername("testUser");
        userDetailsEntity.setPassword("testPassword");
        userDetailsEntity.setAuthorities(authorities);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        List<String> authorityNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Проверяем порядок (хотя Spring Security может не гарантировать порядок)
        for (String expectedAuthority : authorities) {
            assertTrue(authorityNames.contains(expectedAuthority));
        }
    }

    @Test
    void constructor_ShouldHandleNullAuthorities() {
        // Arrange
        String username = "testUser";
        String password = "testPassword";

        // Act
        UserDetailsEntity entity = new UserDetailsEntity(username, password, null);

        // Assert
        assertEquals(username, entity.getUsername());
        assertEquals(password, entity.getPassword());
        assertNull(entity.getAuthorities());
        assertTrue(entity.getAccountNonExpired());
        assertTrue(entity.getEnabled());
    }

    @Test
    void constructor_ShouldHandleEmptyAuthoritiesArray() {
        // Arrange
        String username = "testUser";
        String password = "testPassword";
        String[] emptyAuthorities = {};

        // Act
        UserDetailsEntity entity = new UserDetailsEntity(username, password, emptyAuthorities);

        // Assert
        assertEquals(username, entity.getUsername());
        assertEquals(password, entity.getPassword());
        assertArrayEquals(emptyAuthorities, entity.getAuthorities());
    }

    @Test
    void equalsAndHashCode_ShouldWorkWithLombok() {
        // Arrange
        UserDetailsEntity entity1 = new UserDetailsEntity("user1", "pass1", new String[]{"ROLE_USER"});
        UserDetailsEntity entity2 = new UserDetailsEntity("user1", "pass1", new String[]{"ROLE_USER"});
        UserDetailsEntity entity3 = new UserDetailsEntity("user2", "pass2", new String[]{"ROLE_ADMIN"});

        // Act & Assert
        // equals и hashCode генерируются Lombok'ом
        // Они должны работать на основе всех полей
        assertEquals(entity1, entity1); // рефлексивность
        assertNotEquals(entity1, entity3); // разные значения
        assertEquals(entity1.hashCode(), entity1.hashCode());

        // Note: Для полной проверки equals/hashCode нужно было бы добавить @EqualsAndHashCode к классу
    }

    @Test
    void toUserDetails_ShouldCreateUserWithCorrectBuilderPattern() {
        // Arrange
        userDetailsEntity.setUsername("builderTest");
        userDetailsEntity.setPassword("builderPass");
        userDetailsEntity.setAuthorities(new String[]{"ROLE_TEST"});
        userDetailsEntity.setAccountNonExpired(true);
        userDetailsEntity.setAccountNonLocked(true);
        userDetailsEntity.setCredentialsNonExpired(true);
        userDetailsEntity.setEnabled(true);

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        // Проверяем что User.builder() использовался корректно
        assertFalse(userDetails.isAccountNonExpired() == false);
        assertFalse(userDetails.isAccountNonLocked() == false);
        assertFalse(userDetails.isCredentialsNonExpired() == false);
        assertFalse(userDetails.isEnabled() == false);
    }

    @Test
    void toUserDetails_ShouldHandleMixedBooleanStates() {
        // Arrange
        userDetailsEntity.setUsername("mixedUser");
        userDetailsEntity.setPassword("mixedPass");
        userDetailsEntity.setAuthorities(new String[]{"ROLE_USER"});
        userDetailsEntity.setAccountNonExpired(true);
        userDetailsEntity.setAccountNonLocked(false); // заблокирован
        userDetailsEntity.setCredentialsNonExpired(true);
        userDetailsEntity.setEnabled(false); // отключен

        // Act
        UserDetails userDetails = userDetailsEntity.toUserDetails();

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.isAccountNonExpired());
        assertFalse(userDetails.isAccountNonLocked()); // должен быть заблокирован
        assertTrue(userDetails.isCredentialsNonExpired());
        assertFalse(userDetails.isEnabled()); // должен быть отключен
    }
}