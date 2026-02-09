package com.example.warehouse.infrastructure.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsEntityTest {

    private UserDetailsEntity userDetailsEntity;

    @BeforeEach
    void setUp() {
        userDetailsEntity = new UserDetailsEntity();
    }

    @Test
    void toUserDetails_WhenUsernameIsNull_ShouldReturnNull() {
        UserDetails result = userDetailsEntity.toUserDetails();
        assertNull(result);
    }

    @Test
    void toUserDetails_WhenAllFieldsAreSet_ShouldReturnCompleteUserDetails() {
        String username = "testuser";
        String password = "encodedPassword";
        String[] authorities = {"ROLE_ADMIN", "ROLE_USER"};
        Boolean accountNonExpired = true;
        Boolean accountNonLocked = true;
        Boolean credentialsNonExpired = true;
        Boolean enabled = true;
        
        setUserDetailsEntityField("username", username);
        setUserDetailsEntityField("password", password);
        setUserDetailsEntityField("authorities", authorities);
        setUserDetailsEntityField("accountNonExpired", accountNonExpired);
        setUserDetailsEntityField("accountNonLocked", accountNonLocked);
        setUserDetailsEntityField("credentialsNonExpired", credentialsNonExpired);
        setUserDetailsEntityField("enabled", enabled);
        
        UserDetails result = userDetailsEntity.toUserDetails();
        
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        assertEquals(2, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        assertTrue(result.isEnabled());
    }

    @Test
    void toUserDetails_WhenAccountSettingsAreFalse_ShouldReflectInUserDetails() {
        String username = "testuser";
        Boolean accountNonExpired = false;
        Boolean accountNonLocked = false;
        Boolean credentialsNonExpired = false;
        Boolean enabled = false;
        
        setUserDetailsEntityField("username", username);
        setUserDetailsEntityField("accountNonExpired", accountNonExpired);
        setUserDetailsEntityField("accountNonLocked", accountNonLocked);
        setUserDetailsEntityField("credentialsNonExpired", credentialsNonExpired);
        setUserDetailsEntityField("enabled", enabled);
        
        UserDetails result = userDetailsEntity.toUserDetails();
        
        assertNotNull(result);
        assertFalse(result.isAccountNonExpired());
        assertFalse(result.isAccountNonLocked());
        assertFalse(result.isCredentialsNonExpired());
        assertFalse(result.isEnabled());
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        String username = "testuser";
        String[] authorities = {"ROLE_USER"};
        
        setUserDetailsEntityField("username", username);
        setUserDetailsEntityField("authorities", authorities);
        
        String result = userDetailsEntity.toString();
        
        assertTrue(result.contains("UserDetailsEntity"));
        assertTrue(result.contains(username));
        assertTrue(result.contains("ROLE_USER"));
    }

    // Helper method to set private fields using reflection for testing
    private void setUserDetailsEntityField(String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = UserDetailsEntity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(userDetailsEntity, value);
        } catch (Exception e) {
            fail("Failed to set field " + fieldName + ": " + e.getMessage());
        }
    }
}
