package com.example.feignretryapi.application.usecase;

import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.exception.ExternalApiException;
import com.example.feignretryapi.infrastructure.mock.MockProductGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para GetProductsByCategoryUseCase.
 */
@DisplayName("GetProductsByCategoryUseCase Tests")
class GetProductsByCategoryUseCaseTest {

    private MockProductGateway mockProductGateway;
    private GetProductsByCategoryUseCase useCase;

    @BeforeEach
    void setUp() {
        mockProductGateway = new MockProductGateway();
        useCase = new GetProductsByCategoryUseCase(mockProductGateway);
    }

    @Test
    @DisplayName("Deve retornar produtos da categoria especificada")
    void shouldReturnProductsFromSpecifiedCategory() {
        // Act
        List<Product> result = useCase.execute("electronics");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> 
                p.getCategory().equalsIgnoreCase("electronics")));
        mockProductGateway.verifyFindByCategoryCalled(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando categoria não existe")
    void shouldReturnEmptyListWhenCategoryNotExists() {
        // Act
        List<Product> result = useCase.execute("non-existent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve propagar exceção quando gateway falha")
    void shouldPropagateExceptionWhenGatewayFails() {
        // Arrange
        ExternalApiException expectedException = new ExternalApiException(
                "Rate limit exceeded", 429, true);
        mockProductGateway.setFindByCategoryException(expectedException);

        // Act & Assert
        ExternalApiException thrownException = assertThrows(
                ExternalApiException.class,
                () -> useCase.execute("electronics")
        );

        assertEquals("Rate limit exceeded", thrownException.getMessage());
        assertEquals(429, thrownException.getStatusCode());
    }

    @Test
    @DisplayName("Deve buscar com categoria correta")
    void shouldSearchWithCorrectCategory() {
        // Act
        useCase.execute("furniture");

        // Assert
        assertEquals("furniture", mockProductGateway.getLastFindByCategoryParameter());
    }

    @Test
    @DisplayName("Deve retornar produtos de categoria case-insensitive")
    void shouldReturnProductsCaseInsensitive() {
        // Act
        List<Product> resultUpper = useCase.execute("ELECTRONICS");
        mockProductGateway.reset();
        List<Product> resultLower = useCase.execute("electronics");

        // Assert - O mock já trata case-insensitive
        assertEquals(2, resultUpper.size());
        assertEquals(2, resultLower.size());
    }

    private void verifyFindByCategoryCallCount(int times) {
        mockProductGateway.verifyFindByCategoryCalled(times);
    }
}
