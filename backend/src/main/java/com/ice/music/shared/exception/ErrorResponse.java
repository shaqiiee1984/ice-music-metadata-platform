package com.ice.music.shared.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    String correlationId,
    List<FieldErrorItem> fieldErrors
) {
    public record FieldErrorItem(String field, String message) {}

    public static ErrorResponse of(int status, String error, String message, String path, String correlationId) {
        return new ErrorResponse(Instant.now(), status, error, message, path, correlationId, List.of());
    }

    public static ErrorResponse of(int status, String error, String message, String path, String correlationId, List<FieldErrorItem> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, correlationId, fieldErrors);
    }
}
