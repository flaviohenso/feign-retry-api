package com.example.feignretryapi.application.usecase;

import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.exception.ProductNotFoundException;
import com.example.feignretryapi.domain.gateway.ProductGateway;
import org.springframework.stereotype.Service;

/**
 * Use Case para buscar um produto pelo ID.
 */
@Service
public class GetProductByIdUseCase {

    private final ProductGateway productGateway;

    public GetProductByIdUseCase(ProductGateway productGateway) {
        this.productGateway = productGateway;
    }

    public Product execute(String id) {
        return productGateway.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
