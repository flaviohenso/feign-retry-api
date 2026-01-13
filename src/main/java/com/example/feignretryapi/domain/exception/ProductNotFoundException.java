package com.example.feignretryapi.domain.exception;

/**
 * Exceção lançada quando um produto não é encontrado.
 */
public class ProductNotFoundException extends DomainException {

    public ProductNotFoundException(String id) {
        super("Produto não encontrado com o ID: " + id);
    }
}
