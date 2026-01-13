package com.example.feignretryapi.infrastructure.config;

import com.example.feignretryapi.infrastructure.client.decoder.CustomErrorDecoder;
import com.example.feignretryapi.infrastructure.client.retryer.CustomRetryer;
import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Feign Client.
 */
@Configuration
public class FeignConfig {

    @Value("${external-api.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${external-api.retry.backoff-period:1000}")
    private long backoffPeriod;

    /**
     * Bean para o Retryer customizado.
     */
    @Bean
    public Retryer retryer() {
        return new CustomRetryer(maxAttempts, backoffPeriod);
    }

    /**
     * Bean para o Error Decoder customizado.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    /**
     * Configura o nível de log do Feign.
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
