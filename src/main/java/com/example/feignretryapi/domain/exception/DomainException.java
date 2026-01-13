package com.example.feignretryapi.domain.exception;

/**
 * Exceção base para erros de domínio.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
