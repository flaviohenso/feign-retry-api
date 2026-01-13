package com.example.feignretryapi.presentation.controller;

import com.example.feignretryapi.application.dto.ProductListResponse;
import com.example.feignretryapi.application.dto.ProductResponse;
import com.example.feignretryapi.application.usecase.GetAllProductsUseCase;
import com.example.feignretryapi.application.usecase.GetProductByIdUseCase;
import com.example.feignretryapi.application.usecase.GetProductsByCategoryUseCase;
import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.exception.ProductNotFoundException;
import com.example.feignretryapi.infrastructure.mock.MockProductGateway;
import com.example.feignretryapi.infrastructure.mock.MockProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ProductController.
 */
@DisplayName("ProductController Tests")
class ProductControllerTest {

    private MockProductGateway mockProductGateway;
    private MockProductMapper mockProductMapper;
    private GetAllProductsUseCase getAllProductsUseCase;
    private GetProductByIdUseCase getProductByIdUseCase;
    private GetProductsByCategoryUseCase getProductsByCategoryUseCase;
    private ProductController controller;

    @BeforeEach
    void setUp() {
        mockProductGateway = new MockProductGateway();
        mockProductMapper = new MockProductMapper();
        getAllProductsUseCase = new GetAllProductsUseCase(mockProductGateway);
        getProductByIdUseCase = new GetProductByIdUseCase(mockProductGateway);
        getProductsByCategoryUseCase = new GetProductsByCategoryUseCase(mockProductGateway);
        controller = new ProductController(
                getAllProductsUseCase,
                getProductByIdUseCase,
                getProductsByCategoryUseCase,
                mockProductMapper
        );
    }

    @Test
    @DisplayName("getAllProducts - Deve retornar lista de produtos com status 200")
    void getAllProductsShouldReturnProductsWithStatus200() {
        // Act
        ResponseEntity<ProductListResponse> response = controller.getAllProducts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().total());
        assertEquals(3, response.getBody().products().size());
    }

    @Test
    @DisplayName("getAllProducts - Deve retornar lista vazia quando não há produtos")
    void getAllProductsShouldReturnEmptyListWhenNoProducts() {
        // Arrange
        mockProductGateway.setProducts(List.of());

        // Act
        ResponseEntity<ProductListResponse> response = controller.getAllProducts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().total());
        assertTrue(response.getBody().products().isEmpty());
    }

    @Test
    @DisplayName("getProductById - Deve retornar produto com status 200")
    void getProductByIdShouldReturnProductWithStatus200() {
        // Act
        ResponseEntity<ProductResponse> response = controller.getProductById("1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().id());
        assertEquals("Product 1", response.getBody().name());
    }

    @Test
    @DisplayName("getProductById - Deve lançar exceção quando produto não existe")
    void getProductByIdShouldThrowExceptionWhenProductNotExists() {
        // Act & Assert
        assertThrows(ProductNotFoundException.class, 
                () -> controller.getProductById("999"));
    }

    @Test
    @DisplayName("getProductsByCategory - Deve retornar produtos da categoria")
    void getProductsByCategoryShouldReturnProductsFromCategory() {
        // Act
        ResponseEntity<ProductListResponse> response = controller.getProductsByCategory("electronics");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().total());
    }

    @Test
    @DisplayName("getProductsByCategory - Deve retornar lista vazia para categoria inexistente")
    void getProductsByCategoryShouldReturnEmptyListForNonExistentCategory() {
        // Act
        ResponseEntity<ProductListResponse> response = controller.getProductsByCategory("non-existent");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().total());
    }

    @Test
    @DisplayName("Deve chamar mapper para converter produtos")
    void shouldCallMapperToConvertProducts() {
        // Act
        controller.getAllProducts();

        // Assert
        assertEquals(1, mockProductMapper.getToResponseListCallCount());
    }

    @Test
    @DisplayName("Deve retornar resposta com todos os campos do produto")
    void shouldReturnResponseWithAllProductFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product("test-id", "Test Name", "Test Desc",
                new BigDecimal("99.99"), 10, "test-cat", now, now);
        mockProductGateway.setProducts(List.of(product));

        // Act
        ResponseEntity<ProductListResponse> response = controller.getAllProducts();

        // Assert
        ProductResponse productResponse = response.getBody().products().get(0);
        assertEquals("test-id", productResponse.id());
        assertEquals("Test Name", productResponse.name());
        assertEquals("Test Desc", productResponse.description());
        assertEquals(new BigDecimal("99.99"), productResponse.price());
        assertEquals(10, productResponse.quantity());
        assertEquals("test-cat", productResponse.category());
    }
}
