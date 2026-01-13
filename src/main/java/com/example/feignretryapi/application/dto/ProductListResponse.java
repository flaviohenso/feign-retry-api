package com.example.feignretryapi.application.dto;

import java.util.List;

/**
 * DTO para resposta de lista de produtos.
 */
public record ProductListResponse(
        List<ProductResponse> products,
        int total
) {
}
