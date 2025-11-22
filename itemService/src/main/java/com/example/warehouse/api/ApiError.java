package com.example.warehouse.api;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApiError {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private ErrorCode code;
    private String message;
    private String path;
    private List<String> details;
    private List<ValidationError> validationErrors;

    public ApiError() {
    }

    public ApiError(OffsetDateTime timestamp, int status, String error, ErrorCode code, String message, String path, List<String> details, List<ValidationError> validationErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
        this.details = details;
        this.validationErrors = validationErrors;
    }

    public static ApiError of(int status, String error, ErrorCode code, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, code, message, path, new ArrayList<>(), new ArrayList<>());
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<String> getDetails() {
        return details;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public void addDetail(String detail) {
        if (this.details == null) this.details = new ArrayList<>();
        this.details.add(detail);
    }

    public void addValidationError(String field, Object rejectedValue, String message) {
        if (this.validationErrors == null) this.validationErrors = new ArrayList<>();
        this.validationErrors.add(new ValidationError(field, rejectedValue, message));
    }
}
