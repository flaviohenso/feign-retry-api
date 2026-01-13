package com.example.feignretryapi.infrastructure.mock;

import com.example.feignretryapi.infrastructure.client.feign.ExternalProductClient;
import com.example.feignretryapi.infrastructure.client.feign.dto.ExternalProductDto;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock manual do ExternalProductClient para testes de retry.
 */
public class MockExternalProductClient implements ExternalProductClient {

    private List<ExternalProductDto> products = new ArrayList<>();
    private final AtomicInteger getAllProductsCallCount = new AtomicInteger(0);
    private final AtomicInteger getProductByIdCallCount = new AtomicInteger(0);
    private final AtomicInteger getProductsByCategoryCallCount = new AtomicInteger(0);

    // Configuração de falhas
    private int failGetAllProductsUntilAttempt = 0;
    private int failGetProductByIdUntilAttempt = 0;
    private int failGetProductsByCategoryUntilAttempt = 0;
    private int errorStatusCode = 500;

    private String lastGetProductByIdParameter;
    private String lastGetProductsByCategoryParameter;

    public MockExternalProductClient() {
        initializeDefaultProducts();
    }

    private void initializeDefaultProducts() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        
        products.add(new ExternalProductDto("1", "Product 1", "Description 1",
                new BigDecimal("100.00"), 10, "electronics", now, now));
        products.add(new ExternalProductDto("2", "Product 2", "Description 2",
                new BigDecimal("200.00"), 20, "electronics", now, now));
        products.add(new ExternalProductDto("3", "Product 3", "Description 3",
                new BigDecimal("300.00"), 30, "furniture", now, now));
    }

    @Override
    public List<ExternalProductDto> getAllProducts() {
        int currentCall = getAllProductsCallCount.incrementAndGet();
        
        if (currentCall <= failGetAllProductsUntilAttempt) {
            throw createFeignException(errorStatusCode, "GET", "/api/products");
        }
        
        return new ArrayList<>(products);
    }

    @Override
    public ExternalProductDto getProductById(String id) {
        int currentCall = getProductByIdCallCount.incrementAndGet();
        lastGetProductByIdParameter = id;
        
        if (currentCall <= failGetProductByIdUntilAttempt) {
            throw createFeignException(errorStatusCode, "GET", "/api/products/" + id);
        }
        
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> createFeignException(404, "GET", "/api/products/" + id));
    }

    @Override
    public List<ExternalProductDto> getProductsByCategory(String category) {
        int currentCall = getProductsByCategoryCallCount.incrementAndGet();
        lastGetProductsByCategoryParameter = category;
        
        if (currentCall <= failGetProductsByCategoryUntilAttempt) {
            throw createFeignException(errorStatusCode, "GET", "/api/products/category?category=" + category);
        }
        
        return products.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    private FeignException createFeignException(int status, String method, String url) {
        Request request = Request.create(
                Request.HttpMethod.valueOf(method),
                url,
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        return FeignException.errorStatus(
                "MockExternalProductClient#" + method,
                feign.Response.builder()
                        .status(status)
                        .reason("Mock error")
                        .request(request)
                        .headers(Collections.emptyMap())
                        .build()
        );
    }

    // Métodos de configuração
    public void setProducts(List<ExternalProductDto> products) {
        this.products = new ArrayList<>(products);
    }

    public void addProduct(ExternalProductDto product) {
        this.products.add(product);
    }

    public void setFailGetAllProductsUntilAttempt(int failUntilAttempt) {
        this.failGetAllProductsUntilAttempt = failUntilAttempt;
    }

    public void setFailGetProductByIdUntilAttempt(int failUntilAttempt) {
        this.failGetProductByIdUntilAttempt = failUntilAttempt;
    }

    public void setFailGetProductsByCategoryUntilAttempt(int failUntilAttempt) {
        this.failGetProductsByCategoryUntilAttempt = failUntilAttempt;
    }

    public void setErrorStatusCode(int statusCode) {
        this.errorStatusCode = statusCode;
    }

    // Métodos de verificação
    public int getGetAllProductsCallCount() {
        return getAllProductsCallCount.get();
    }

    public int getGetProductByIdCallCount() {
        return getProductByIdCallCount.get();
    }

    public int getGetProductsByCategoryCallCount() {
        return getProductsByCategoryCallCount.get();
    }

    public String getLastGetProductByIdParameter() {
        return lastGetProductByIdParameter;
    }

    public String getLastGetProductsByCategoryParameter() {
        return lastGetProductsByCategoryParameter;
    }

    public void reset() {
        products.clear();
        initializeDefaultProducts();
        getAllProductsCallCount.set(0);
        getProductByIdCallCount.set(0);
        getProductsByCategoryCallCount.set(0);
        failGetAllProductsUntilAttempt = 0;
        failGetProductByIdUntilAttempt = 0;
        failGetProductsByCategoryUntilAttempt = 0;
        errorStatusCode = 500;
        lastGetProductByIdParameter = null;
        lastGetProductsByCategoryParameter = null;
    }

    public void verifyGetAllProductsCalled(int times) {
        if (getAllProductsCallCount.get() != times) {
            throw new AssertionError(
                    String.format("Expected getAllProducts() to be called %d times, but was called %d times",
                            times, getAllProductsCallCount.get()));
        }
    }

    public void verifyGetProductByIdCalled(int times) {
        if (getProductByIdCallCount.get() != times) {
            throw new AssertionError(
                    String.format("Expected getProductById() to be called %d times, but was called %d times",
                            times, getProductByIdCallCount.get()));
        }
    }

    public void verifyGetProductsByCategoryCalled(int times) {
        if (getProductsByCategoryCallCount.get() != times) {
            throw new AssertionError(
                    String.format("Expected getProductsByCategory() to be called %d times, but was called %d times",
                            times, getProductsByCategoryCallCount.get()));
        }
    }
}
