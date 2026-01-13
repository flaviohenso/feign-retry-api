package com.example.feignretryapi.application.usecase;

import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.exception.ExternalApiException;
import com.example.feignretryapi.infrastructure.mock.MockProductGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para GetAllProductsUseCase.
 */
@DisplayName("GetAllProductsUseCase Tests")
class GetAllProductsUseCaseTest {

    private MockProductGateway mockProductGateway;
    private GetAllProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        mockProductGateway = new MockProductGateway();
        useCase = new GetAllProductsUseCase(mockProductGateway);
    }

    @Test
    @DisplayName("Deve retornar lista de produtos com sucesso")
    void shouldReturnProductsSuccessfully() {
        // Act
        List<Product> result = useCase.execute();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        mockProductGateway.verifyFindAllCalled(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos")
    void shouldReturnEmptyListWhenNoProducts() {
        // Arrange
        mockProductGateway.setProducts(List.of());

        // Act
        List<Product> result = useCase.execute();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        mockProductGateway.verifyFindAllCalled(1);
    }

    @Test
    @DisplayName("Deve propagar exceção quando gateway falha")
    void shouldPropagateExceptionWhenGatewayFails() {
        // Arrange
        ExternalApiException expectedException = new ExternalApiException(
                "Erro de conexão", 500, true);
        mockProductGateway.setFindAllException(expectedException);

        // Act & Assert
        ExternalApiException thrownException = assertThrows(
                ExternalApiException.class,
                () -> useCase.execute()
        );

        assertEquals("Erro de conexão", thrownException.getMessage());
        assertEquals(500, thrownException.getStatusCode());
        mockProductGateway.verifyFindAllCalled(1);
    }

    @Test
    @DisplayName("Deve retornar produtos na ordem correta")
    void shouldReturnProductsInCorrectOrder() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<Product> orderedProducts = List.of(
                new Product("1", "First", "Desc", BigDecimal.ONE, 1, "cat", now, now),
                new Product("2", "Second", "Desc", BigDecimal.TEN, 2, "cat", now, now)
        );
        mockProductGateway.setProducts(orderedProducts);

        // Act
        List<Product> result = useCase.execute();

        // Assert
        assertEquals(2, result.size());
        assertEquals("First", result.get(0).getName());
        assertEquals("Second", result.get(1).getName());
    }
}
