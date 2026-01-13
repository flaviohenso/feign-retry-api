# Feign Retry API

API Spring Boot com Java 21 que demonstra o uso de Feign Client com mecanismo de retry customizado usando decoder e retryer do Feign.

## Tecnologias

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring Cloud OpenFeign**
- **MapStruct** - Mapeamento de objetos
- **JUnit 5** - Testes unitários
- **Clean Architecture**

## Arquitetura

O projeto segue a Clean Architecture com as seguintes camadas:

```
src/main/java/com/example/feignretryapi/
├── domain/                    # Camada de Domínio
│   ├── entity/               # Entidades de negócio
│   ├── exception/            # Exceções de domínio
│   └── gateway/              # Interfaces de gateway
├── application/               # Camada de Aplicação
│   ├── dto/                  # DTOs de entrada/saída
│   └── usecase/              # Casos de uso
├── infrastructure/            # Camada de Infraestrutura
│   ├── client/               # Clientes externos (Feign)
│   │   ├── decoder/          # Decoder customizado
│   │   ├── feign/            # Feign clients
│   │   └── retryer/          # Retryer customizado
│   ├── config/               # Configurações
│   ├── gateway/              # Implementações de gateway
│   ├── mapper/               # Mappers (MapStruct)
│   └── mock/                 # Mock da API externa
└── presentation/              # Camada de Apresentação
    └── controller/           # Controllers REST
```

## Mecanismo de Retry

### CustomErrorDecoder

O `CustomErrorDecoder` determina quais erros são retryable:

- **Retryable (permitem retry):**
  - Erros 5xx (500, 502, 503, etc.)
  - Erro 429 (Rate Limit)
  - Erro 408 (Timeout)

- **Não Retryable:**
  - Erros 4xx (exceto 429 e 408)
  - Erro 404 retorna `ProductNotFoundException`

### CustomRetryer

O `CustomRetryer` implementa:

- Número máximo de tentativas configurável
- Backoff exponencial entre tentativas
- Logging de cada tentativa

### Configuração

```yaml
external-api:
  base-url: http://localhost:8081
  retry:
    max-attempts: 3
    backoff-period: 1000  # ms
```

## Como Executar

### Modo Normal (API externa real)

```bash
mvn spring-boot:run
```

### Modo Mock (API externa mockada)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

## Endpoints

### API Principal (porta 8080)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/products` | Lista todos os produtos |
| GET | `/api/v1/products/{id}` | Busca produto por ID |
| GET | `/api/v1/products/category/{category}` | Busca produtos por categoria |

### Mock API (porta 8080, profile mock)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/products/mock/config` | Configura comportamento de falha |
| POST | `/api/products/mock/reset` | Reseta configurações do mock |
| GET | `/api/products/mock/health` | Verifica saúde do mock |

### Testando o Retry

Com o profile `mock` ativo, você pode configurar falhas para testar o retry:

```bash
# Configurar para falhar nas primeiras 2 requisições
curl -X POST "http://localhost:8080/api/products/mock/config?endpoint=getAllProducts&errorCode=503&failUntilAttempt=2"

# Fazer requisição (retry será executado automaticamente)
curl http://localhost:8080/api/v1/products

# Resetar configurações
curl -X POST http://localhost:8080/api/products/mock/reset
```

## Testes

### Executar todos os testes

```bash
mvn test
```

### Testes por camada

```bash
# Testes de Use Cases
mvn test -Dtest="*UseCaseTest"

# Testes de Infrastructure
mvn test -Dtest="*DecoderTest,*RetryerTest,*GatewayImplTest"

# Testes de Controller
mvn test -Dtest="*ControllerTest"
```

## Estrutura de Testes

Os testes utilizam **mocks manuais** sem uso de bibliotecas como Mockito:

- `MockProductGateway` - Mock do gateway de produtos
- `MockProductMapper` - Mock do mapper
- `MockExternalProductClient` - Mock do cliente Feign

### Exemplo de Mock Manual

```java
public class MockProductGateway implements ProductGateway {
    private RuntimeException findAllException;
    private int findAllCallCount = 0;
    
    @Override
    public List<Product> findAll() {
        findAllCallCount++;
        if (findAllException != null) {
            throw findAllException;
        }
        return products;
    }
    
    // Métodos de verificação
    public void verifyFindAllCalled(int times) {
        if (findAllCallCount != times) {
            throw new AssertionError("...");
        }
    }
}
```

## Build

```bash
# Compilar
mvn clean compile

# Empacotar
mvn clean package

# Executar JAR
java -jar target/feign-retry-api-1.0.0-SNAPSHOT.jar
```

## Autor

Desenvolvido como exemplo de implementação de retry com Feign Client usando Clean Architecture.
