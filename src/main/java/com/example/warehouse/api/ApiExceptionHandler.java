package com.example.warehouse.api;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest req) {
        List<ValidationError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ValidationError.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Validation failed", req, null, fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest req) {
        List<ValidationError> fields = ex.getConstraintViolations().stream()
                .map(cv -> ValidationError.builder()
                        .field(extractProperty(cv))
                        .rejectedValue(cv.getInvalidValue())
                        .message(cv.getMessage())
                        .build())
                .toList();

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Validation failed", req, null, fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                             HttpServletRequest req) {
        List<String> details = new ArrayList<>();
        Throwable cause = ex.getMostSpecificCause();
        String msg = "Malformed JSON request";

        if (cause instanceof InvalidFormatException ife) {
            msg = "Invalid JSON value";
            details.add(pathOf(ife) + ": " + simpleMessage(cause.getMessage()));
        } else if (cause instanceof JsonMappingException jme) {
            details.add(pathOf(jme) + ": " + simpleMessage(cause.getMessage()));
        } else if (cause != null) {
            details.add(simpleMessage(cause.getMessage()));
        }

        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", msg, req, details, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest req) {
        String param = ex.getName();
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        List<String> details = List.of("Parameter '" + param + "' must be of type '" + expected + "'");
        List<ValidationError> fields = List.of(ValidationError.builder()
                .field(param)
                .rejectedValue(ex.getValue())
                .message("must be of type '" + expected + "'")
                .build());
        return build(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH",
                "Request parameter has wrong type", req, details, fields);
    }

    @ExceptionHandler({
            EntityNotFoundException.class,
            NoSuchElementException.class,
            EmptyResultDataAccessException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "Entity not found", req, one(simpleMessage(ex.getMessage())), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest req) {
        String root = simpleMessage(rootCause(ex).getMessage());
        return build(HttpStatus.CONFLICT, "DB_INTEGRITY",
                "Data integrity violation", req, one(root), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        String root = simpleMessage(rootCause(ex).getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Unexpected error", req, one(root), null);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status,
                                                   String code,
                                                   String message,
                                                   HttpServletRequest req,
                                                   List<String> details,
                                                   List<ValidationError> fields) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(req.getRequestURI())
                .details(isEmpty(details) ? null : details)
                .fields(isEmpty(fields) ? null : fields)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private static String extractProperty(ConstraintViolation<?> cv) {
        String path = cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "";
        return path;
    }

    private static String pathOf(JsonMappingException jme) {
        if (jme.getPath() == null || jme.getPath().isEmpty()) return "$";
        return "$." + jme.getPath().stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .reduce((a, b) -> a + "." + b).orElse("$");
    }

    private static Throwable rootCause(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null && r.getCause() != r) {
            r = r.getCause();
        }
        return r;
    }

    private static String simpleMessage(String m) {
        if (m == null) return null;
        int nl = m.indexOf('\n');
        return nl > 0 ? m.substring(0, nl).trim() : m.trim();
    }

    private static <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    private static List<String> one(String s) {
        return s == null || s.isBlank() ? null : List.of(s);
    }
}
