package com.example.feignretryapi.infrastructure.mock;

import com.example.feignretryapi.infrastructure.client.feign.dto.ExternalProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock Controller que simula a API externa de produtos.
 * Ativado apenas com o profile "mock".
 * 
 * Este mock permite testar:
 * - Respostas normais
 * - Erros 5xx para testar retry
 * - Rate limiting (429)
 * - Timeouts
 */
@RestController
@RequestMapping("/api/products")
@Profile("mock")
public class MockExternalApiController {

    private static final Logger logger = LoggerFactory.getLogger(MockExternalApiController.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    // Contador para simular falhas intermitentes
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    
    // Configuração de comportamento do mock
    private final Map<String, MockBehavior> mockBehaviors = new ConcurrentHashMap<>();

    // Dados mock de produtos
    private final List<ExternalProductDto> mockProducts;

    public MockExternalApiController() {
        this.mockProducts = initializeMockProducts();
    }

    private List<ExternalProductDto> initializeMockProducts() {
        List<ExternalProductDto> products = new ArrayList<>();
        String now = LocalDateTime.now().format(formatter);

        products.add(new ExternalProductDto(
                "1", "Notebook Dell XPS 15", "Notebook premium com tela 4K",
                new BigDecimal("8999.99"), 10, "electronics", now, now
        ));

        products.add(new ExternalProductDto(
                "2", "iPhone 15 Pro", "Smartphone Apple última geração",
                new BigDecimal("9499.00"), 25, "electronics", now, now
        ));

        products.add(new ExternalProductDto(
                "3", "Cadeira Gamer RGB", "Cadeira ergonômica para gamers",
                new BigDecimal("1299.90"), 50, "furniture", now, now
        ));

        products.add(new ExternalProductDto(
                "4", "Monitor LG 34\" Ultrawide", "Monitor curvo para produtividade",
                new BigDecimal("3499.00"), 15, "electronics", now, now
        ));

        products.add(new ExternalProductDto(
                "5", "Teclado Mecânico Keychron", "Teclado mecânico wireless",
                new BigDecimal("699.00"), 100, "electronics", now, now
        ));

        return products;
    }

    /**
     * Retorna todos os produtos mock.
     */
    @GetMapping
    public ResponseEntity<List<ExternalProductDto>> getAllProducts() {
        int currentRequest = requestCounter.incrementAndGet();
        logger.info("Mock API - GET /api/products - Requisição #{}", currentRequest);

        // Simula comportamento de falha intermitente se configurado
        MockBehavior behavior = mockBehaviors.get("getAllProducts");
        if (behavior != null && behavior.shouldFail(currentRequest)) {
            logger.warn("Mock API - Simulando falha {} para requisição #{}", 
                    behavior.getErrorCode(), currentRequest);
            return ResponseEntity.status(behavior.getErrorCode()).build();
        }

        // Simula latência
        simulateLatency(100);

        return ResponseEntity.ok(mockProducts);
    }

    /**
     * Retorna um produto pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExternalProductDto> getProductById(@PathVariable String id) {
        int currentRequest = requestCounter.incrementAndGet();
        logger.info("Mock API - GET /api/products/{} - Requisição #{}", id, currentRequest);

        // Simula comportamento de falha
        MockBehavior behavior = mockBehaviors.get("getProductById");
        if (behavior != null && behavior.shouldFail(currentRequest)) {
            logger.warn("Mock API - Simulando falha {} para requisição #{}", 
                    behavior.getErrorCode(), currentRequest);
            return ResponseEntity.status(behavior.getErrorCode()).build();
        }

        // Simula latência
        simulateLatency(50);

        return mockProducts.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retorna produtos por categoria.
     */
    @GetMapping("/category")
    public ResponseEntity<List<ExternalProductDto>> getProductsByCategory(
            @RequestParam("category") String category) {
        int currentRequest = requestCounter.incrementAndGet();
        logger.info("Mock API - GET /api/products/category?category={} - Requisição #{}", 
                category, currentRequest);

        // Simula comportamento de falha
        MockBehavior behavior = mockBehaviors.get("getProductsByCategory");
        if (behavior != null && behavior.shouldFail(currentRequest)) {
            logger.warn("Mock API - Simulando falha {} para requisição #{}", 
                    behavior.getErrorCode(), currentRequest);
            return ResponseEntity.status(behavior.getErrorCode()).build();
        }

        // Simula latência
        simulateLatency(75);

        List<ExternalProductDto> filtered = mockProducts.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .toList();

        return ResponseEntity.ok(filtered);
    }

    /**
     * Endpoint para configurar comportamento de falha do mock.
     * Útil para testes de retry.
     */
    @PostMapping("/mock/config")
    public ResponseEntity<String> configureMockBehavior(
            @RequestParam String endpoint,
            @RequestParam int errorCode,
            @RequestParam int failUntilAttempt) {
        
        MockBehavior behavior = new MockBehavior(errorCode, failUntilAttempt);
        mockBehaviors.put(endpoint, behavior);
        
        logger.info("Mock configurado: endpoint={}, errorCode={}, failUntilAttempt={}", 
                endpoint, errorCode, failUntilAttempt);
        
        return ResponseEntity.ok("Mock configurado com sucesso");
    }

    /**
     * Endpoint para resetar configurações do mock.
     */
    @PostMapping("/mock/reset")
    public ResponseEntity<String> resetMockBehavior() {
        mockBehaviors.clear();
        requestCounter.set(0);
        logger.info("Mock resetado");
        return ResponseEntity.ok("Mock resetado com sucesso");
    }

    /**
     * Endpoint para verificar saúde do mock.
     */
    @GetMapping("/mock/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "requestCount", requestCounter.get(),
                "activeBehaviors", mockBehaviors.size()
        ));
    }

    private void simulateLatency(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Classe interna para configurar comportamento de falha do mock.
     */
    private static class MockBehavior {
        private final int errorCode;
        private final int failUntilAttempt;

        public MockBehavior(int errorCode, int failUntilAttempt) {
            this.errorCode = errorCode;
            this.failUntilAttempt = failUntilAttempt;
        }

        public boolean shouldFail(int currentAttempt) {
            return currentAttempt <= failUntilAttempt;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }
}
