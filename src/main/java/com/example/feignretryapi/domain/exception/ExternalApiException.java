package com.example.feignretryapi.domain.exception;

/**
 * Exceção lançada quando ocorre erro na comunicação com API externa.
 */
public class ExternalApiException extends DomainException {

    private final int statusCode;
    private final boolean retryable;

    public ExternalApiException(String message, int statusCode, boolean retryable) {
        super(message);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    public ExternalApiException(String message, int statusCode, boolean retryable, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
