package com.example.feignretryapi.infrastructure.client.retryer;

import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import feign.Retryer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CustomRetryer.
 */
@DisplayName("CustomRetryer Tests")
class CustomRetryerTest {

    private CustomRetryer retryer;
    private static final int MAX_ATTEMPTS = 3;
    private static final long BACKOFF_PERIOD = 100;

    @BeforeEach
    void setUp() {
        retryer = new CustomRetryer(MAX_ATTEMPTS, BACKOFF_PERIOD);
    }

    @Test
    @DisplayName("Deve permitir retry quando tentativas não foram excedidas")
    void shouldAllowRetryWhenAttemptsNotExceeded() {
        // Arrange
        RetryableException exception = createRetryableException();

        // Act & Assert - Primeira tentativa (attempt 2)
        assertDoesNotThrow(() -> retryer.continueOrPropagate(exception));
        assertEquals(2, retryer.getCurrentAttempt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando máximo de tentativas é alcançado")
    void shouldThrowExceptionWhenMaxAttemptsReached() {
        // Arrange
        RetryableException exception = createRetryableException();

        // Act - Executa até o limite
        retryer.continueOrPropagate(exception); // attempt 2
        retryer.continueOrPropagate(exception); // attempt 3 (max)

        // Assert - Próxima tentativa deve falhar
        assertThrows(RetryableException.class, 
                () -> retryer.continueOrPropagate(exception));
    }

    @Test
    @DisplayName("Deve incrementar contador de tentativas corretamente")
    void shouldIncrementAttemptCounterCorrectly() {
        // Arrange
        RetryableException exception = createRetryableException();

        // Assert initial state
        assertEquals(1, retryer.getCurrentAttempt());

        // Act & Assert
        retryer.continueOrPropagate(exception);
        assertEquals(2, retryer.getCurrentAttempt());

        retryer.continueOrPropagate(exception);
        assertEquals(3, retryer.getCurrentAttempt());
    }

    @Test
    @DisplayName("Deve clonar corretamente para novas requisições")
    void shouldCloneCorrectlyForNewRequests() {
        // Arrange
        RetryableException exception = createRetryableException();
        retryer.continueOrPropagate(exception); // Incrementa contador

        // Act
        Retryer clonedRetryer = retryer.clone();

        // Assert - Clone deve começar do início
        assertInstanceOf(CustomRetryer.class, clonedRetryer);
        CustomRetryer customClone = (CustomRetryer) clonedRetryer;
        assertEquals(1, customClone.getCurrentAttempt());
        assertEquals(MAX_ATTEMPTS, customClone.getMaxAttempts());
        assertEquals(BACKOFF_PERIOD, customClone.getBackoffPeriod());
    }

    @Test
    @DisplayName("Deve aplicar backoff entre tentativas")
    void shouldApplyBackoffBetweenAttempts() {
        // Arrange
        RetryableException exception = createRetryableException();
        long startTime = System.currentTimeMillis();

        // Act
        retryer.continueOrPropagate(exception);
        long endTime = System.currentTimeMillis();

        // Assert - Deve ter aguardado pelo menos o backoff period
        long elapsed = endTime - startTime;
        assertTrue(elapsed >= BACKOFF_PERIOD, 
                "Deveria ter aguardado pelo menos " + BACKOFF_PERIOD + "ms, mas aguardou " + elapsed + "ms");
    }

    @Test
    @DisplayName("Deve retornar configurações corretas")
    void shouldReturnCorrectConfiguration() {
        // Assert
        assertEquals(MAX_ATTEMPTS, retryer.getMaxAttempts());
        assertEquals(BACKOFF_PERIOD, retryer.getBackoffPeriod());
    }

    @Test
    @DisplayName("Deve funcionar com diferentes configurações")
    void shouldWorkWithDifferentConfigurations() {
        // Arrange
        CustomRetryer customRetryer = new CustomRetryer(5, 50);
        RetryableException exception = createRetryableException();

        // Act & Assert
        assertEquals(5, customRetryer.getMaxAttempts());
        assertEquals(50, customRetryer.getBackoffPeriod());

        // Deve permitir mais tentativas
        for (int i = 0; i < 4; i++) {
            assertDoesNotThrow(() -> customRetryer.continueOrPropagate(exception));
        }

        // Na quinta tentativa deve falhar
        assertThrows(RetryableException.class, 
                () -> customRetryer.continueOrPropagate(exception));
    }

    private RetryableException createRetryableException() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/products",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        return new RetryableException(
                500,
                "Server Error",
                Request.HttpMethod.GET,
                new Date(),
                request
        );
    }
}
