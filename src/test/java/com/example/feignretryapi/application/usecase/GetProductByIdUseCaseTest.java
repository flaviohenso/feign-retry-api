package com.example.feignretryapi.application.usecase;

import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.exception.ExternalApiException;
import com.example.feignretryapi.domain.exception.ProductNotFoundException;
import com.example.feignretryapi.infrastructure.mock.MockProductGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para GetProductByIdUseCase.
 */
@DisplayName("GetProductByIdUseCase Tests")
class GetProductByIdUseCaseTest {

    private MockProductGateway mockProductGateway;
    private GetProductByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        mockProductGateway = new MockProductGateway();
        useCase = new GetProductByIdUseCase(mockProductGateway);
    }

    @Test
    @DisplayName("Deve retornar produto quando encontrado")
    void shouldReturnProductWhenFound() {
        // Act
        Product result = useCase.execute("1");

        // Assert
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Product 1", result.getName());
        mockProductGateway.verifyFindByIdCalled(1);
        mockProductGateway.verifyFindByIdCalledWith("1");
    }

    @Test
    @DisplayName("Deve lançar ProductNotFoundException quando produto não existe")
    void shouldThrowProductNotFoundExceptionWhenProductNotExists() {
        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> useCase.execute("999")
        );

        assertTrue(exception.getMessage().contains("999"));
        mockProductGateway.verifyFindByIdCalled(1);
        mockProductGateway.verifyFindByIdCalledWith("999");
    }

    @Test
    @DisplayName("Deve propagar exceção quando gateway falha")
    void shouldPropagateExceptionWhenGatewayFails() {
        // Arrange
        ExternalApiException expectedException = new ExternalApiException(
                "Timeout", 408, true);
        mockProductGateway.setFindByIdException(expectedException);

        // Act & Assert
        ExternalApiException thrownException = assertThrows(
                ExternalApiException.class,
                () -> useCase.execute("1")
        );

        assertEquals("Timeout", thrownException.getMessage());
        assertEquals(408, thrownException.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar produto com todos os campos preenchidos")
    void shouldReturnProductWithAllFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Product expectedProduct = new Product(
                "test-id", "Test Product", "Test Description",
                new BigDecimal("999.99"), 50, "test-category", now, now
        );
        mockProductGateway.setCustomFindByIdBehavior(id -> 
                id.equals("test-id") ? Optional.of(expectedProduct) : Optional.empty()
        );

        // Act
        Product result = useCase.execute("test-id");

        // Assert
        assertEquals("test-id", result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(new BigDecimal("999.99"), result.getPrice());
        assertEquals(50, result.getQuantity());
        assertEquals("test-category", result.getCategory());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve buscar com ID correto")
    void shouldSearchWithCorrectId() {
        // Act
        useCase.execute("2");

        // Assert
        assertEquals("2", mockProductGateway.getLastFindByIdParameter());
    }
}
