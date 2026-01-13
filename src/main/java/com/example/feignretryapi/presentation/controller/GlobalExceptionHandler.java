package com.example.feignretryapi.presentation.controller;

import com.example.feignretryapi.application.dto.ErrorResponse;
import com.example.feignretryapi.domain.exception.DomainException;
import com.example.feignretryapi.domain.exception.ExternalApiException;
import com.example.feignretryapi.domain.exception.ProductNotFoundException;
import feign.FeignException;
import feign.RetryableException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler global de exceções para a API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex, HttpServletRequest request) {
        logger.warn("Produto não encontrado: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                "PRODUCT_NOT_FOUND",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            ExternalApiException ex, HttpServletRequest request) {
        logger.error("Erro na API externa: {} - Status: {}", ex.getMessage(), ex.getStatusCode());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                "EXTERNAL_API_ERROR",
                request.getRequestURI()
        );
        
        HttpStatus status = ex.getStatusCode() >= 500 
                ? HttpStatus.SERVICE_UNAVAILABLE 
                : HttpStatus.BAD_GATEWAY;
        
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ErrorResponse> handleRetryableException(
            RetryableException ex, HttpServletRequest request) {
        logger.error("Erro retryable após todas as tentativas: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                "Serviço temporariamente indisponível. Todas as tentativas de retry falharam.",
                "SERVICE_UNAVAILABLE",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex, HttpServletRequest request) {
        logger.error("Erro Feign: {} - Status: {}", ex.getMessage(), ex.status());
        
        ErrorResponse errorResponse = new ErrorResponse(
                "Erro na comunicação com serviço externo: " + ex.getMessage(),
                "FEIGN_ERROR",
                request.getRequestURI()
        );
        
        HttpStatus status = ex.status() >= 500 
                ? HttpStatus.SERVICE_UNAVAILABLE 
                : HttpStatus.BAD_GATEWAY;
        
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex, HttpServletRequest request) {
        logger.error("Erro de domínio: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                "DOMAIN_ERROR",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Erro inesperado: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                "Erro interno do servidor",
                "INTERNAL_ERROR",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
