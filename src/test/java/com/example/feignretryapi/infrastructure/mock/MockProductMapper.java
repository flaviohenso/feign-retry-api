package com.example.feignretryapi.infrastructure.mock;

import com.example.feignretryapi.application.dto.ProductResponse;
import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.infrastructure.client.feign.dto.ExternalProductDto;
import com.example.feignretryapi.infrastructure.mapper.ProductMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mock manual do ProductMapper para testes.
 */
public class MockProductMapper implements ProductMapper {

    private int toDomainCallCount = 0;
    private int toDomainListCallCount = 0;
    private int toResponseCallCount = 0;
    private int toResponseListCallCount = 0;

    @Override
    public Product toDomain(ExternalProductDto dto) {
        toDomainCallCount++;
        
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setCategory(dto.getCategory());
        product.setCreatedAt(stringToLocalDateTime(dto.getCreatedAt()));
        product.setUpdatedAt(stringToLocalDateTime(dto.getUpdatedAt()));
        
        return product;
    }

    @Override
    public List<Product> toDomainList(List<ExternalProductDto> dtos) {
        toDomainListCallCount++;
        
        if (dtos == null) {
            return List.of();
        }
        
        return dtos.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse toResponse(Product product) {
        toResponseCallCount++;
        
        if (product == null) {
            return null;
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    @Override
    public List<ProductResponse> toResponseList(List<Product> products) {
        toResponseListCallCount++;
        
        if (products == null) {
            return List.of();
        }
        
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LocalDateTime stringToLocalDateTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public String localDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    // Métodos de verificação
    public int getToDomainCallCount() {
        return toDomainCallCount;
    }

    public int getToDomainListCallCount() {
        return toDomainListCallCount;
    }

    public int getToResponseCallCount() {
        return toResponseCallCount;
    }

    public int getToResponseListCallCount() {
        return toResponseListCallCount;
    }

    public void reset() {
        toDomainCallCount = 0;
        toDomainListCallCount = 0;
        toResponseCallCount = 0;
        toResponseListCallCount = 0;
    }
}
