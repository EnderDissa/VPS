package com.example.warehouse.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.warehouse.exception.AccessDeniedException;
import com.example.warehouse.exception.BorrowingNotFoundException;
import com.example.warehouse.exception.BusinessRuleException;
import com.example.warehouse.exception.ConflictException;
import com.example.warehouse.exception.DuplicateKeepingException;
import com.example.warehouse.exception.DuplicateLicensePlateException;
import com.example.warehouse.exception.DuplicateSerialNumberException;
import com.example.warehouse.exception.DuplicateStorageException;
import com.example.warehouse.exception.DuplicateUserStorageAccessException;
import com.example.warehouse.exception.ItemMaintenanceNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.KeepingNotFoundException;
import com.example.warehouse.exception.OperationNotAllowedException;
import com.example.warehouse.exception.StorageNotEmptyException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.TransportationNotFoundException;
import com.example.warehouse.exception.UserAlreadyExistsException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.exception.UserStorageAccessNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.exception.VehicleNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.EntityNotFoundException;
import reactor.core.publisher.Mono;

@WebFluxTest(
        controllers = DummyController.class,
        useDefaultFilters = false
)
@Import(ApiExceptionHandler.class)
class ApiExceptionHandlerTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private DummyController dummyController;

    static Stream<Arguments> exceptionProvider() {
        return Stream.of(
                Arguments.of(new AccessDeniedException("Access denied"), 403, "FORBIDDEN"),
                Arguments.of(new BorrowingNotFoundException("Borrowing not found"), 404, "BORROWING_NOT_FOUND"),
                Arguments.of(new BusinessRuleException("Business rule violated"), 400, "BUSINESS_RULE_VIOLATION"),
                Arguments.of(new ConflictException("Resource conflict"), 409, "CONFLICT"),
                Arguments.of(new DuplicateKeepingException("Duplicate keeping"), 409, "DUPLICATE_KEEPING"),
                Arguments.of(new DuplicateLicensePlateException("Duplicate license plate"), 409, "DUPLICATE_LICENSE_PLATE"),
                Arguments.of(new DuplicateSerialNumberException("Duplicate serial number"), 409, "DUPLICATE_SERIAL_NUMBER"),
                Arguments.of(new DuplicateStorageException("Duplicate storage"), 409, "DUPLICATE_STORAGE"),
                Arguments.of(new DuplicateUserStorageAccessException("Duplicate user storage access"), 409, "DUPLICATE_USER_STORAGE_ACCESS"),
                Arguments.of(new ItemMaintenanceNotFoundException("Item maintenance not found"), 404, "ITEM_MAINTENANCE_NOT_FOUND"),
                Arguments.of(new ItemNotFoundException("Item not found"), 404, "ITEM_NOT_FOUND"),
                Arguments.of(new KeepingNotFoundException("Keeping not found"), 404, "KEEPING_NOT_FOUND"),
                Arguments.of(new OperationNotAllowedException("Operation not allowed"), 403, "OPERATION_NOT_ALLOWED"),
                Arguments.of(new StorageNotEmptyException("Storage not empty"), 409, "STORAGE_NOT_EMPTY"),
                Arguments.of(new StorageNotFoundException("Storage not found"), 404, "STORAGE_NOT_FOUND"),
                Arguments.of(new TransportationNotFoundException("Transportation not found"), 404, "TRANSPORTATION_NOT_FOUND"),
                Arguments.of(new UserAlreadyExistsException("User already exists"), 409, "USER_ALREADY_EXISTS"),
                Arguments.of(new UserNotFoundException("User not found"), 404, "USER_NOT_FOUND"),
                Arguments.of(new UserStorageAccessNotFoundException("User storage access not found"), 404, "USER_STORAGE_ACCESS_NOT_FOUND"),
                Arguments.of(new ValidationException("Validation failed"), 400, "VALIDATION_ERROR"),
                Arguments.of(new VehicleNotFoundException("Vehicle not found"), 404, "VEHICLE_NOT_FOUND")
        );
    }

    @ParameterizedTest
    @MethodSource("exceptionProvider")
    void handlesCustomExceptions(Exception ex, int status, String errorCode) {
        when(dummyController.throwException(any())).thenReturn(Mono.error(ex));

        client.get().uri("/error")
                .exchange()
                .expectStatus().isEqualTo(status)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(status)
                .jsonPath("$.code").isEqualTo(errorCode)
                .jsonPath("$.path").isEqualTo("/error");
    }


    @Test
    void handlesWebExchangeBindException() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "must be valid email"));
        bindingResult.addError(new FieldError("target", "age", "must be positive"));

        WebExchangeBindException ex = new WebExchangeBindException(
                new MethodParameter(Mockito.mock(Method.class), -1), bindingResult
        );

        when(dummyController.throwException(any())).thenReturn(Mono.error(ex));

        client.get().uri("/error")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                .jsonPath("$.validationErrors.length()").isEqualTo(2)
                .jsonPath("$.validationErrors[0].field").isEqualTo("email")
                .jsonPath("$.validationErrors[1].field").isEqualTo("age");
    }

    @Test
    void handlesServerWebInputException() {
        ServerWebInputException ex = new ServerWebInputException("Required query parameter 'id' is missing");

        when(dummyController.throwException(any())).thenReturn(Mono.error(ex));

        client.get().uri("/error")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_JSON")
                .jsonPath("$.details[0]").isEqualTo("Required query parameter 'id' is missing");
    }

    @Test
    void handlesUnexpectedError() {
        when(dummyController.throwException(any())).thenReturn(Mono.error(new RuntimeException("System failure")));

        client.get().uri("/error")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void handlesInvalidJSON() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Required query parameter 'id' is missing");

        when(dummyController.throwException(any())).thenReturn(Mono.error(ex));

        client.get().uri("/error")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_JSON")
                .jsonPath("$.details[0]").isEqualTo("Required query parameter 'id' is missing");
    }

    @Test
    void handleTypeMismatch() {
        EntityNotFoundException ex = new EntityNotFoundException("Required query parameter 'id' is missing");

        when(dummyController.throwException(any())).thenReturn(Mono.error(ex));

        client.get().uri("/error")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("NOT_FOUND");
    }

    private BindingResult mockBindingResult() {
        BindingResult result = mock(BindingResult.class);
        when(result.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "testField", "Invalid value")
        ));
        return result;
    }
}

@RestController
class DummyController {
    @GetMapping("/error")
    Mono<String> throwException(ServerWebExchange exchange) {
        throw new RuntimeException("Should be caught by handler");
    }
}