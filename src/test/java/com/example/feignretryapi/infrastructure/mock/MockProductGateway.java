package com.example.feignretryapi.infrastructure.mock;

import com.example.feignretryapi.domain.entity.Product;
import com.example.feignretryapi.domain.gateway.ProductGateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Mock manual do ProductGateway para testes.
 */
public class MockProductGateway implements ProductGateway {

    private List<Product> products = new ArrayList<>();
    private RuntimeException findAllException;
    private RuntimeException findByIdException;
    private RuntimeException findByCategoryException;

    // Contadores de chamadas
    private int findAllCallCount = 0;
    private int findByIdCallCount = 0;
    private int findByCategoryCallCount = 0;
    private String lastFindByIdParameter;
    private String lastFindByCategoryParameter;

    // Comportamento customizado
    private Supplier<List<Product>> customFindAllBehavior;
    private Function<String, Optional<Product>> customFindByIdBehavior;
    private Function<String, List<Product>> customFindByCategoryBehavior;

    public MockProductGateway() {
        initializeDefaultProducts();
    }

    private void initializeDefaultProducts() {
        LocalDateTime now = LocalDateTime.now();
        
        products.add(new Product("1", "Product 1", "Description 1", 
                new BigDecimal("100.00"), 10, "electronics", now, now));
        products.add(new Product("2", "Product 2", "Description 2", 
                new BigDecimal("200.00"), 20, "electronics", now, now));
        products.add(new Product("3", "Product 3", "Description 3", 
                new BigDecimal("300.00"), 30, "furniture", now, now));
    }

    @Override
    public List<Product> findAll() {
        findAllCallCount++;
        
        if (findAllException != null) {
            throw findAllException;
        }
        
        if (customFindAllBehavior != null) {
            return customFindAllBehavior.get();
        }
        
        return new ArrayList<>(products);
    }

    @Override
    public Optional<Product> findById(String id) {
        findByIdCallCount++;
        lastFindByIdParameter = id;
        
        if (findByIdException != null) {
            throw findByIdException;
        }
        
        if (customFindByIdBehavior != null) {
            return customFindByIdBehavior.apply(id);
        }
        
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Product> findByCategory(String category) {
        findByCategoryCallCount++;
        lastFindByCategoryParameter = category;
        
        if (findByCategoryException != null) {
            throw findByCategoryException;
        }
        
        if (customFindByCategoryBehavior != null) {
            return customFindByCategoryBehavior.apply(category);
        }
        
        return products.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    // Métodos para configurar o mock
    public void setProducts(List<Product> products) {
        this.products = new ArrayList<>(products);
    }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public void setFindAllException(RuntimeException exception) {
        this.findAllException = exception;
    }

    public void setFindByIdException(RuntimeException exception) {
        this.findByIdException = exception;
    }

    public void setFindByCategoryException(RuntimeException exception) {
        this.findByCategoryException = exception;
    }

    public void setCustomFindAllBehavior(Supplier<List<Product>> behavior) {
        this.customFindAllBehavior = behavior;
    }

    public void setCustomFindByIdBehavior(Function<String, Optional<Product>> behavior) {
        this.customFindByIdBehavior = behavior;
    }

    public void setCustomFindByCategoryBehavior(Function<String, List<Product>> behavior) {
        this.customFindByCategoryBehavior = behavior;
    }

    // Métodos para verificação
    public int getFindAllCallCount() {
        return findAllCallCount;
    }

    public int getFindByIdCallCount() {
        return findByIdCallCount;
    }

    public int getFindByCategoryCallCount() {
        return findByCategoryCallCount;
    }

    public String getLastFindByIdParameter() {
        return lastFindByIdParameter;
    }

    public String getLastFindByCategoryParameter() {
        return lastFindByCategoryParameter;
    }

    public void reset() {
        products.clear();
        initializeDefaultProducts();
        findAllException = null;
        findByIdException = null;
        findByCategoryException = null;
        customFindAllBehavior = null;
        customFindByIdBehavior = null;
        customFindByCategoryBehavior = null;
        findAllCallCount = 0;
        findByIdCallCount = 0;
        findByCategoryCallCount = 0;
        lastFindByIdParameter = null;
        lastFindByCategoryParameter = null;
    }

    public void verifyFindAllCalled(int times) {
        if (findAllCallCount != times) {
            throw new AssertionError(
                    String.format("Expected findAll() to be called %d times, but was called %d times", 
                            times, findAllCallCount));
        }
    }

    public void verifyFindByIdCalled(int times) {
        if (findByIdCallCount != times) {
            throw new AssertionError(
                    String.format("Expected findById() to be called %d times, but was called %d times", 
                            times, findByIdCallCount));
        }
    }

    public void verifyFindByIdCalledWith(String id) {
        if (!id.equals(lastFindByIdParameter)) {
            throw new AssertionError(
                    String.format("Expected findById() to be called with '%s', but was called with '%s'", 
                            id, lastFindByIdParameter));
        }
    }

    public void verifyFindByCategoryCalled(int times) {
        if (findByCategoryCallCount != times) {
            throw new AssertionError(
                    String.format("Expected findByCategory() to be called %d times, but was called %d times", 
                            times, findByCategoryCallCount));
        }
    }

    public void verifyFindByCategoryCalledWith(String category) {
        if (!category.equals(lastFindByCategoryParameter)) {
            throw new AssertionError(
                    String.format("Expected findByCategory() to be called with '%s', but was called with '%s'", 
                            category, lastFindByCategoryParameter));
        }
    }
}
