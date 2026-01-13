package com.example.feignretryapi.application.usecase;

import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.gateway.ProductGateway;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use Case para buscar todos os produtos.
 */
@Service
public class GetAllProductsUseCase {

    private final ProductGateway productGateway;

    public GetAllProductsUseCase(ProductGateway productGateway) {
        this.productGateway = productGateway;
    }

    public List<Product> execute() {
        return productGateway.findAll();
    }
}
