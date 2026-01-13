package com.example.feignretryapi.infrastructure.client.decoder;

import com.example.feignretryapi.domain.exception.ExternalApiException;
import com.example.feignretryapi.domain.exception.ProductNotFoundException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decoder customizado para tratar erros do Feign Client.
 * Determina quais erros são retryable e transforma em exceções apropriadas.
 */
public class CustomErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorDecoder.class);

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String reason = response.reason() != null ? response.reason() : "Unknown error";

        logger.error("Erro na chamada ao método {}: Status {}, Reason: {}", methodKey, status, reason);

        // Erros 5xx são considerados retryable
        if (isServerError(status)) {
            logger.warn("Erro de servidor ({}). Marcando como retryable.", status);
            return createRetryableException(response, methodKey, 
                    "Erro de servidor: " + status + " - " + reason);
        }

        // Erro 429 (Too Many Requests) também é retryable
        if (status == 429) {
            logger.warn("Rate limit excedido. Marcando como retryable.");
            return createRetryableException(response, methodKey, 
                    "Rate limit excedido. Aguarde antes de tentar novamente.");
        }

        // Erro 408 (Request Timeout) é retryable
        if (status == 408) {
            logger.warn("Timeout na requisição. Marcando como retryable.");
            return createRetryableException(response, methodKey, 
                    "Timeout na requisição.");
        }

        // Erro 404 - Recurso não encontrado
        if (status == 404) {
            logger.info("Recurso não encontrado no método: {}", methodKey);
            return new ProductNotFoundException(extractIdFromMethodKey(methodKey));
        }

        // Erros 4xx não são retryable (exceto os tratados acima)
        if (isClientError(status)) {
            logger.error("Erro de cliente ({}). Não será realizado retry.", status);
            return new ExternalApiException(
                    "Erro na requisição: " + status + " - " + reason,
                    status,
                    false
            );
        }

        // Para outros erros, usa o decoder padrão
        return defaultErrorDecoder.decode(methodKey, response);
    }

    /**
     * Cria uma exceção RetryableException para permitir retry pelo Feign.
     */
    private RetryableException createRetryableException(Response response, String methodKey, String message) {
        return new RetryableException(
                response.status(),
                message,
                response.request().httpMethod(),
                new Date(),
                response.request()
        );
    }

    /**
     * Verifica se o status é um erro de servidor (5xx).
     */
    private boolean isServerError(int status) {
        return status >= 500 && status < 600;
    }

    /**
     * Verifica se o status é um erro de cliente (4xx).
     */
    private boolean isClientError(int status) {
        return status >= 400 && status < 500;
    }

    /**
     * Extrai o ID do recurso do methodKey, se disponível.
     */
    private String extractIdFromMethodKey(String methodKey) {
        // Implementação simplificada - em produção, fazer parsing mais robusto
        return "unknown";
    }
}
