package com.example.feignretryapi.application.usecase;

import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.gateway.ProductGateway;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use Case para buscar produtos por categoria.
 */
@Service
public class GetProductsByCategoryUseCase {

    private final ProductGateway productGateway;

    public GetProductsByCategoryUseCase(ProductGateway productGateway) {
        this.productGateway = productGateway;
    }

    public List<Product> execute(String category) {
        return productGateway.findByCategory(category);
    }
}
