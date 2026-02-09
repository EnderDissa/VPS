package com.example.warehouse.infrastructure.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestTypeTest {

    @Test
    void enumValues_ShouldExist() {
        RequestType[] expectedValues = {RequestType.REGISTER, RequestType.GET_USER, RequestType.CHANGE_PASSWORD};
        RequestType[] actualValues = RequestType.values();
        
        assertArrayEquals(expectedValues, actualValues);
    }

    @Test
    void enumValueOf_ShouldReturnCorrectEnum() {
        assertEquals(RequestType.REGISTER, RequestType.valueOf("REGISTER"));
        assertEquals(RequestType.GET_USER, RequestType.valueOf("GET_USER"));
        assertEquals(RequestType.CHANGE_PASSWORD, RequestType.valueOf("CHANGE_PASSWORD"));
    }
}
