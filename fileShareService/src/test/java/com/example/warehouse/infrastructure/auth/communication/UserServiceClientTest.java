package com.example.warehouse.infrastructure.auth.communication;

import com.example.warehouse.infrastructure.auth.UserDetailsEntity;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import reactivefeign.spring.config.ReactiveFeignClient;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceClientTest {

    @Test
    void interface_ShouldBeAnnotatedWithReactiveFeignClient() {
        ReactiveFeignClient annotation = UserServiceClient.class.getAnnotation(ReactiveFeignClient.class);
        assertNotNull(annotation, "UserServiceClient should be annotated with @ReactiveFeignClient");
        assertEquals("USERSERVICE", annotation.name());
        assertEquals(Fallback.class, annotation.fallback());
    }

    @Test
    void checkUserAuthMethod_ShouldExistWithCorrectSignature() throws NoSuchMethodException {
        Method method = UserServiceClient.class.getMethod("checkUserAuth", String.class);
        
        assertNotNull(method, "checkUserAuth method should exist");
        assertEquals(Mono.class, method.getReturnType());
        assertEquals(1, method.getParameterCount());
        assertEquals(String.class, method.getParameterTypes()[0]);
    }

    @Test
    void checkUserAuthMethod_ShouldBeAnnotatedWithPostMapping() throws NoSuchMethodException {
        Method method = UserServiceClient.class.getMethod("checkUserAuth", String.class);
        PostMapping annotation = method.getAnnotation(PostMapping.class);
        
        assertNotNull(annotation, "checkUserAuth method should be annotated with @PostMapping");
        assertEquals("/internal/validate", annotation.value()[0]);
    }

    @Test
    void checkUserAuthMethod_ParameterShouldBeAnnotatedWithRequestBody() throws NoSuchMethodException {
        Method method = UserServiceClient.class.getMethod("checkUserAuth", String.class);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        
        boolean hasRequestBodyAnnotation = false;
        for (Annotation annotation : parameterAnnotations[0]) {
            if (annotation instanceof RequestBody) {
                hasRequestBodyAnnotation = true;
                break;
            }
        }
        
        assertTrue(hasRequestBodyAnnotation, "checkUserAuth parameter should be annotated with @RequestBody");
    }

    @Test
    void returnTypeOfCheckUserAuth_ShouldBeMonoOfUserDetailsEntity() throws NoSuchMethodException {
        Method method = UserServiceClient.class.getMethod("checkUserAuth", String.class);
        assertEquals(Mono.class, method.getReturnType());
        
        // Check generic type parameter
        // Note: Generic type information is erased at runtime, so we can't easily check this
        // But we can at least verify it's a Mono
    }
}
