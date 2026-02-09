package com.example.warehouse.infrastructure.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class AuthRequestTest {

    private AuthRequest authRequest;
    private static final String TEST_ID = "test-id";
    private static final RequestType TEST_TYPE = RequestType.GET_USER;
    private static final String TEST_PAYLOAD = "test-payload";

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest(TEST_ID, TEST_TYPE, TEST_PAYLOAD);
    }

    @Test
    void constructorWithId_ShouldSetFieldsCorrectly() {
        assertEquals(TEST_ID, authRequest.getId());
        assertEquals(TEST_TYPE, authRequest.getType());
        assertEquals(TEST_PAYLOAD, authRequest.getPayload());
        assertEquals(AuthRequest.CREATED, authRequest.getStatus());
        assertNull(authRequest.getResult());
    }

    @Test
    void constructorWithoutId_ShouldGenerateIdAndSetFieldsCorrectly() {
        AuthRequest request = new AuthRequest(TEST_TYPE, TEST_PAYLOAD);
        
        assertNotNull(request.getId());
        assertEquals(TEST_TYPE, request.getType());
        assertEquals(TEST_PAYLOAD, request.getPayload());
        assertEquals(AuthRequest.CREATED, request.getStatus());
        assertNull(request.getResult());
    }

    @Test
    void setResult_WhenStatusIsCreated_ShouldSetResultAndStatus() {
        String resultPayload = "result";
        authRequest.setResult(true, resultPayload);
        
        assertEquals(AuthRequest.SUCCESS, authRequest.getStatus());
        assertEquals(resultPayload, authRequest.getResult());
    }

    @Test
    void setResult_WhenStatusIsCreatedAndSuccessFalse_ShouldSetErrorStatus() {
        String resultPayload = "error-message";
        authRequest.setResult(false, resultPayload);
        
        assertEquals(AuthRequest.ERROR, authRequest.getStatus());
        assertEquals(resultPayload, authRequest.getResult());
    }

    @Test
    void setResult_WhenCalledTwice_ShouldThrowIllegalStateException() {
        authRequest.setResult(true, "result");
        
        assertThrows(IllegalStateException.class, () -> {
            authRequest.setResult(true, "another-result");
        });
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        String result = authRequest.toString();
        
        assertTrue(result.contains("AuthRequest"));
        assertTrue(result.contains(TEST_TYPE.toString()));
        assertTrue(result.contains(TEST_PAYLOAD));
        assertTrue(result.contains(TEST_ID));
    }

    @Test
    void responseConstructor_ShouldSetFieldsCorrectly() {
        int status = AuthRequest.SUCCESS;
        String result = "test-result";
        
        AuthRequest.Response response = new AuthRequest.Response(status, result);
        
        assertEquals(status, response.getStatus());
        assertEquals(result, response.getResult());
    }

    @Test
    void responseToString_ShouldReturnFormattedString() {
        AuthRequest.Response response = new AuthRequest.Response(AuthRequest.SUCCESS, "test-result");
        String result = response.toString();
        
        assertTrue(result.contains("Response"));
        assertTrue(result.contains("1")); // SUCCESS = 1
        assertTrue(result.contains("test-result"));
    }
}
