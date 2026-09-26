package br.com.socialconnect.api.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ProblemDetail(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    LocalDateTime timestamp,
    List<FieldError> errors
) {
    public record FieldError(
        String field,
        String message
    ) {}
}
