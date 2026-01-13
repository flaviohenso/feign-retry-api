package com.example.feignretryapi.presentation.controller;

import com.example.feignretryapi.application.dto.ProductListResponse;
import com.example.feignretryapi.application.dto.ProductResponse;
import com.example.feignretryapi.application.usecase.GetAllProductsUseCase;
import com.example.feignretryapi.application.usecase.GetProductByIdUseCase;
import com.example.feignretryapi.application.usecase.GetProductsByCategoryUseCase;
import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.infrastructure.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operações com produtos.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final GetAllProductsUseCase getAllProductsUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final GetProductsByCategoryUseCase getProductsByCategoryUseCase;
    private final ProductMapper productMapper;

    public ProductController(
            GetAllProductsUseCase getAllProductsUseCase,
            GetProductByIdUseCase getProductByIdUseCase,
            GetProductsByCategoryUseCase getProductsByCategoryUseCase,
            ProductMapper productMapper
    ) {
        this.getAllProductsUseCase = getAllProductsUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.getProductsByCategoryUseCase = getProductsByCategoryUseCase;
        this.productMapper = productMapper;
    }

    /**
     * Busca todos os produtos.
     *
     * @return Lista de produtos
     */
    @GetMapping
    public ResponseEntity<ProductListResponse> getAllProducts() {
        logger.info("Requisição para buscar todos os produtos");
        
        List<Product> products = getAllProductsUseCase.execute();
        List<ProductResponse> productResponses = productMapper.toResponseList(products);
        
        ProductListResponse response = new ProductListResponse(productResponses, productResponses.size());
        
        logger.info("Retornando {} produtos", productResponses.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um produto pelo ID.
     *
     * @param id Identificador do produto
     * @return Produto encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        logger.info("Requisição para buscar produto com ID: {}", id);
        
        Product product = getProductByIdUseCase.execute(id);
        ProductResponse response = productMapper.toResponse(product);
        
        logger.info("Retornando produto: {}", response.name());
        return ResponseEntity.ok(response);
    }

    /**
     * Busca produtos por categoria.
     *
     * @param category Categoria dos produtos
     * @return Lista de produtos da categoria
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ProductListResponse> getProductsByCategory(@PathVariable String category) {
        logger.info("Requisição para buscar produtos da categoria: {}", category);
        
        List<Product> products = getProductsByCategoryUseCase.execute(category);
        List<ProductResponse> productResponses = productMapper.toResponseList(products);
        
        ProductListResponse response = new ProductListResponse(productResponses, productResponses.size());
        
        logger.info("Retornando {} produtos da categoria {}", productResponses.size(), category);
        return ResponseEntity.ok(response);
    }
}
