package com.example.warehouse.api;

import java.util.List;

public record ApiErrorResponse(String code, String message, List<ApiErrorField> fields) {}
