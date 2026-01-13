package com.example.feignretryapi.application.dto;

import java.time.LocalDateTime;

/**
 * DTO para resposta de erro.
 */
public record ErrorResponse(
        String message,
        String code,
        LocalDateTime timestamp,
        String path
) {
    public ErrorResponse(String message, String code, String path) {
        this(message, code, LocalDateTime.now(), path);
    }
}
