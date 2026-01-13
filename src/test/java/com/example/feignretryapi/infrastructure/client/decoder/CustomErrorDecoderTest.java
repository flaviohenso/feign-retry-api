package com.example.feignretryapi.infrastructure.client.decoder;

import com.example.feignretryapi.domain.exception.ExternalApiException;
import com.example.feignretryapi.domain.exception.ProductNotFoundException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CustomErrorDecoder.
 */
@DisplayName("CustomErrorDecoder Tests")
class CustomErrorDecoderTest {

    private CustomErrorDecoder errorDecoder;

    @BeforeEach
    void setUp() {
        errorDecoder = new CustomErrorDecoder();
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 501, 502, 503, 504})
    @DisplayName("Deve retornar RetryableException para erros 5xx")
    void shouldReturnRetryableExceptionForServerErrors(int statusCode) {
        // Arrange
        Response response = createResponse(statusCode, "Server Error");

        // Act
        Exception result = errorDecoder.decode("TestMethod#test", response);

        // Assert
        assertInstanceOf(RetryableException.class, result);
        RetryableException retryableException = (RetryableException) result;
        assertEquals(statusCode, retryableException.status());
    }

    @Test
    @DisplayName("Deve retornar RetryableException para erro 429 (Rate Limit)")
    void shouldReturnRetryableExceptionForRateLimit() {
        // Arrange
        Response response = createResponse(429, "Too Many Requests");

        // Act
        Exception result = errorDecoder.decode("TestMethod#test", response);

        // Assert
        assertInstanceOf(RetryableException.class, result);
        assertTrue(result.getMessage().contains("Rate limit"));
    }

    @Test
    @DisplayName("Deve retornar RetryableException para erro 408 (Timeout)")
    void shouldReturnRetryableExceptionForTimeout() {
        // Arrange
        Response response = createResponse(408, "Request Timeout");

        // Act
        Exception result = errorDecoder.decode("TestMethod#test", response);

        // Assert
        assertInstanceOf(RetryableException.class, result);
        assertTrue(result.getMessage().contains("Timeout"));
    }

    @Test
    @DisplayName("Deve retornar ProductNotFoundException para erro 404")
    void shouldReturnProductNotFoundExceptionFor404() {
        // Arrange
        Response response = createResponse(404, "Not Found");

        // Act
        Exception result = errorDecoder.decode("TestMethod#getProductById", response);

        // Assert
        assertInstanceOf(ProductNotFoundException.class, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 405, 422})
    @DisplayName("Deve retornar ExternalApiException não-retryable para outros erros 4xx")
    void shouldReturnNonRetryableExceptionForOtherClientErrors(int statusCode) {
        // Arrange
        Response response = createResponse(statusCode, "Client Error");

        // Act
        Exception result = errorDecoder.decode("TestMethod#test", response);

        // Assert
        assertInstanceOf(ExternalApiException.class, result);
        ExternalApiException apiException = (ExternalApiException) result;
        assertFalse(apiException.isRetryable());
        assertEquals(statusCode, apiException.getStatusCode());
    }

    @Test
    @DisplayName("Deve incluir mensagem de erro na exceção")
    void shouldIncludeErrorMessageInException() {
        // Arrange
        Response response = createResponse(500, "Internal Server Error");

        // Act
        Exception result = errorDecoder.decode("TestMethod#getAllProducts", response);

        // Assert
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("500") || 
                   result.getMessage().contains("servidor"));
    }

    @Test
    @DisplayName("Deve tratar response sem reason")
    void shouldHandleResponseWithoutReason() {
        // Arrange
        Response response = createResponse(500, null);

        // Act
        Exception result = errorDecoder.decode("TestMethod#test", response);

        // Assert
        assertInstanceOf(RetryableException.class, result);
        assertNotNull(result.getMessage());
    }

    private Response createResponse(int status, String reason) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/products",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        return Response.builder()
                .status(status)
                .reason(reason)
                .request(request)
                .headers(Collections.emptyMap())
                .build();
    }
}
