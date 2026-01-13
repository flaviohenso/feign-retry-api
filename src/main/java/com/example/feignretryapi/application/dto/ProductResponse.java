package com.example.feignretryapi.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para resposta de produto.
 */
public record ProductResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
