package com.example.feignretryapi.infrastructure.gateway;

import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.exception.ExternalApiException;
import com.example.feignretryapi.infrastructure.client.feign.dto.ExternalProductDto;
import com.example.feignretryapi.infrastructure.mock.MockExternalProductClient;
import com.example.feignretryapi.infrastructure.mock.MockProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ProductGatewayImpl.
 */
@DisplayName("ProductGatewayImpl Tests")
class ProductGatewayImplTest {

    private MockExternalProductClient mockClient;
    private MockProductMapper mockMapper;
    private ProductGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        mockClient = new MockExternalProductClient();
        mockMapper = new MockProductMapper();
        gateway = new ProductGatewayImpl(mockClient, mockMapper);
    }

    @Test
    @DisplayName("findAll - Deve retornar lista de produtos com sucesso")
    void findAllShouldReturnProductsSuccessfully() {
        // Act
        List<Product> result = gateway.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        mockClient.verifyGetAllProductsCalled(1);
        assertEquals(1, mockMapper.getToDomainListCallCount());
    }

    @Test
    @DisplayName("findAll - Deve lançar exceção quando cliente falha")
    void findAllShouldThrowExceptionWhenClientFails() {
        // Arrange
        mockClient.setFailGetAllProductsUntilAttempt(10); // Sempre falha
        mockClient.setErrorStatusCode(500);

        // Act & Assert
        assertThrows(ExternalApiException.class, () -> gateway.findAll());
    }

    @Test
    @DisplayName("findById - Deve retornar produto quando encontrado")
    void findByIdShouldReturnProductWhenFound() {
        // Act
        Optional<Product> result = gateway.findById("1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        mockClient.verifyGetProductByIdCalled(1);
        assertEquals(1, mockMapper.getToDomainCallCount());
    }

    @Test
    @DisplayName("findById - Deve retornar empty quando produto não existe")
    void findByIdShouldReturnEmptyWhenProductNotExists() {
        // Act
        Optional<Product> result = gateway.findById("999");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findById - Deve lançar exceção para erros de servidor")
    void findByIdShouldThrowExceptionForServerErrors() {
        // Arrange
        mockClient.setFailGetProductByIdUntilAttempt(10);
        mockClient.setErrorStatusCode(503);

        // Act & Assert
        assertThrows(ExternalApiException.class, () -> gateway.findById("1"));
    }

    @Test
    @DisplayName("findByCategory - Deve retornar produtos da categoria")
    void findByCategoryShouldReturnProductsFromCategory() {
        // Act
        List<Product> result = gateway.findByCategory("electronics");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        mockClient.verifyGetProductsByCategoryCalled(1);
    }

    @Test
    @DisplayName("findByCategory - Deve retornar lista vazia para categoria inexistente")
    void findByCategoryShouldReturnEmptyListForNonExistentCategory() {
        // Act
        List<Product> result = gateway.findByCategory("non-existent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByCategory - Deve lançar exceção quando cliente falha")
    void findByCategoryShouldThrowExceptionWhenClientFails() {
        // Arrange
        mockClient.setFailGetProductsByCategoryUntilAttempt(10);
        mockClient.setErrorStatusCode(500);

        // Act & Assert
        assertThrows(ExternalApiException.class, 
                () -> gateway.findByCategory("electronics"));
    }

    @Test
    @DisplayName("Deve mapear corretamente os campos do DTO para entidade")
    void shouldMapDtoFieldsCorrectlyToEntity() {
        // Arrange
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        ExternalProductDto dto = new ExternalProductDto(
                "test-id", "Test Name", "Test Desc",
                new BigDecimal("99.99"), 5, "test-cat", now, now
        );
        mockClient.setProducts(List.of(dto));

        // Act
        List<Product> result = gateway.findAll();

        // Assert
        assertEquals(1, result.size());
        Product product = result.get(0);
        assertEquals("test-id", product.getId());
        assertEquals("Test Name", product.getName());
        assertEquals("Test Desc", product.getDescription());
        assertEquals(new BigDecimal("99.99"), product.getPrice());
        assertEquals(5, product.getQuantity());
        assertEquals("test-cat", product.getCategory());
    }

    private void verifyGetProductsByCategoryCalled(int times) {
        if (mockClient.getGetProductsByCategoryCallCount() != times) {
            throw new AssertionError(
                    String.format("Expected getProductsByCategory() to be called %d times, but was called %d times",
                            times, mockClient.getGetProductsByCategoryCallCount()));
        }
    }
}
