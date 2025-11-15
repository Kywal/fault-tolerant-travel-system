package br.ufrn.imd.travel.circuit;

import lombok.extern.slf4j.Slf4j;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class CircuitBreaker {
    
    private enum State {
        CLOSED,  
        OPEN,       
        HALF_OPEN   
    }
    
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>();
    
    private final int failureThreshold;
    private final int successThreshold;
    private final Duration timeout;
    private final String name;
    
    public CircuitBreaker(String name, int failureThreshold, int successThreshold, Duration timeout) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.timeout = timeout;
    }
    
    public <T> T execute(CircuitBreakerSupplier<T> supplier) throws Exception {
        State currentState = getState();
        
        if (currentState == State.OPEN) {
            log.error("[CircuitBreaker:{}] OPEN - Requisição bloqueada", name);
            throw new CircuitBreakerOpenException("Circuit breaker está aberto para " + name);
        }
        
        try {
            T result = supplier.get();
            onSuccess();
            return result;
            
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
    
    private State getState() {
        State current = state.get();
        
        if (current == State.OPEN) {
            Instant lastFail = lastFailureTime.get();
            if (lastFail != null && Duration.between(lastFail, Instant.now()).compareTo(timeout) > 0) {
                log.info("[CircuitBreaker:{}] Timeout expirado. Mudando para HALF_OPEN", name);
                state.compareAndSet(State.OPEN, State.HALF_OPEN);
                return State.HALF_OPEN;
            }
        }
        
        return current;
    }
    
    private void onSuccess() {
        State currentState = state.get();
        
        if (currentState == State.HALF_OPEN) {
            int successes = successCount.incrementAndGet();
            log.info("[CircuitBreaker:{}] Sucesso em HALF_OPEN ({}/{})", name, successes, successThreshold);
            
            if (successes >= successThreshold) {
                log.info("[CircuitBreaker:{}] Mudando para CLOSED", name);
                state.set(State.CLOSED);
                failureCount.set(0);
                successCount.set(0);
            }
        } else if (currentState == State.CLOSED) {
            failureCount.set(0);
        }
    }
    
    private void onFailure() {
        State currentState = state.get();
        lastFailureTime.set(Instant.now());
        
        if (currentState == State.HALF_OPEN) {
            log.warn("[CircuitBreaker:{}] Falha em HALF_OPEN. Voltando para OPEN", name);
            state.set(State.OPEN);
            successCount.set(0);
            
        } else if (currentState == State.CLOSED) {
            int failures = failureCount.incrementAndGet();
            log.warn("[CircuitBreaker:{}] Falha detectada ({}/{})", name, failures, failureThreshold);
            
            if (failures >= failureThreshold) {
                log.error("[CircuitBreaker:{}] Threshold atingido. Mudando para OPEN", name);
                state.set(State.OPEN);
            }
        }
    }
    
    public State getCurrentState() {
        return getState();
    }
    
    public void reset() {
        log.info("[CircuitBreaker:{}] Reset manual", name);
        state.set(State.CLOSED);
        failureCount.set(0);
        successCount.set(0);
    }
    
    @FunctionalInterface
    public interface CircuitBreakerSupplier<T> {
        T get() throws Exception;
    }
    
    public static class CircuitBreakerOpenException extends Exception {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}