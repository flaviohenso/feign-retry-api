package com.example.feignretryapi.infrastructure.client.retryer;

import feign.RetryableException;
import feign.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementação customizada do Feign Retryer.
 * Realiza tentativas de retry com backoff exponencial.
 */
public class CustomRetryer implements Retryer {

    private static final Logger logger = LoggerFactory.getLogger(CustomRetryer.class);

    private final int maxAttempts;
    private final long backoffPeriod;
    private int attempt;

    public CustomRetryer(int maxAttempts, long backoffPeriod) {
        this.maxAttempts = maxAttempts;
        this.backoffPeriod = backoffPeriod;
        this.attempt = 1;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt >= maxAttempts) {
            logger.error("Número máximo de tentativas alcançado ({}) para a requisição. Erro: {}", 
                    maxAttempts, e.getMessage());
            throw e;
        }

        attempt++;
        long waitTime = calculateBackoff();
        
        logger.warn("Tentativa {} de {}. Aguardando {}ms antes do próximo retry. Motivo: {}", 
                attempt, maxAttempts, waitTime, e.getMessage());

        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    /**
     * Calcula o tempo de espera com backoff exponencial.
     * 
     * @return tempo de espera em milissegundos
     */
    private long calculateBackoff() {
        // Backoff exponencial: backoffPeriod * 2^(attempt-1)
        return backoffPeriod * (long) Math.pow(2, attempt - 1);
    }

    @Override
    public Retryer clone() {
        return new CustomRetryer(maxAttempts, backoffPeriod);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getBackoffPeriod() {
        return backoffPeriod;
    }

    public int getCurrentAttempt() {
        return attempt;
    }
}
