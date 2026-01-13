package com.example.feignretryapi.infrastructure.client.feign;

import com.example.feignretryapi.infrastructure.client.feign.dto.ExternalProductDto;
import com.example.feignretryapi.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign Client para comunicação com a API externa de produtos.
 */
@FeignClient(
        name = "external-product-api",
        url = "${external-api.base-url}",
        configuration = FeignConfig.class
)
public interface ExternalProductClient {

    @GetMapping("/api/products")
    List<ExternalProductDto> getAllProducts();

    @GetMapping("/api/products/{id}")
    ExternalProductDto getProductById(@PathVariable("id") String id);

    @GetMapping("/api/products/category")
    List<ExternalProductDto> getProductsByCategory(@RequestParam("category") String category);
}
