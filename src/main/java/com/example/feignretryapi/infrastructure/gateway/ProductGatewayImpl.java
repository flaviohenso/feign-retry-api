package com.example.feignretryapi.infrastructure.gateway;

import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.exception.ExternalApiException;
import com.example.feignretryapi.domain.exception.ProductNotFoundException;
import com.example.feignretryapi.domain.gateway.ProductGateway;
import com.example.feignretryapi.infrastructure.client.feign.ExternalProductClient;
import com.example.feignretryapi.infrastructure.client.feign.dto.ExternalProductDto;
import com.example.feignretryapi.infrastructure.mapper.ProductMapper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementação do ProductGateway que utiliza o Feign Client.
 */
@Component
public class ProductGatewayImpl implements ProductGateway {

    private static final Logger logger = LoggerFactory.getLogger(ProductGatewayImpl.class);

    private final ExternalProductClient externalProductClient;
    private final ProductMapper productMapper;

    public ProductGatewayImpl(ExternalProductClient externalProductClient, ProductMapper productMapper) {
        this.externalProductClient = externalProductClient;
        this.productMapper = productMapper;
    }

    @Override
    public List<Product> findAll() {
        logger.info("Buscando todos os produtos da API externa");
        try {
            List<ExternalProductDto> externalProducts = externalProductClient.getAllProducts();
            logger.info("Encontrados {} produtos", externalProducts.size());
            return productMapper.toDomainList(externalProducts);
        } catch (FeignException e) {
            logger.error("Erro ao buscar produtos da API externa: {}", e.getMessage());
            throw new ExternalApiException(
                    "Falha ao buscar produtos da API externa",
                    e.status(),
                    false,
                    e
            );
        }
    }

    @Override
    public Optional<Product> findById(String id) {
        logger.info("Buscando produto com ID: {}", id);
        try {
            ExternalProductDto externalProduct = externalProductClient.getProductById(id);
            Product product = productMapper.toDomain(externalProduct);
            logger.info("Produto encontrado: {}", product.getName());
            return Optional.of(product);
        } catch (ProductNotFoundException e) {
            logger.warn("Produto não encontrado com ID: {}", id);
            return Optional.empty();
        } catch (FeignException.NotFound e) {
            logger.warn("Produto não encontrado com ID: {}", id);
            return Optional.empty();
        } catch (FeignException e) {
            logger.error("Erro ao buscar produto com ID {}: {}", id, e.getMessage());
            throw new ExternalApiException(
                    "Falha ao buscar produto da API externa",
                    e.status(),
                    false,
                    e
            );
        }
    }

    @Override
    public List<Product> findByCategory(String category) {
        logger.info("Buscando produtos da categoria: {}", category);
        try {
            List<ExternalProductDto> externalProducts = externalProductClient.getProductsByCategory(category);
            logger.info("Encontrados {} produtos na categoria {}", externalProducts.size(), category);
            return productMapper.toDomainList(externalProducts);
        } catch (FeignException e) {
            logger.error("Erro ao buscar produtos da categoria {}: {}", category, e.getMessage());
            throw new ExternalApiException(
                    "Falha ao buscar produtos por categoria da API externa",
                    e.status(),
                    false,
                    e
            );
        }
    }
}
