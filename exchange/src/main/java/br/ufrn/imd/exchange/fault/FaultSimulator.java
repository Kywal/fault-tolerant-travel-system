package br.ufrn.imd.exchange.fault;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FaultSimulator {
    
    private final Random random = new Random();
    private final ConcurrentHashMap<String, Long> faultStates = new ConcurrentHashMap<>();
    
    public void simulateFault(String requestName, FaultConfig config) throws FaultException {
        String stateKey = requestName + "_" + Thread.currentThread().getId();
        
        Long faultEndTime = faultStates.get(stateKey);
        if (faultEndTime != null && System.currentTimeMillis() < faultEndTime) {
            long remainingTime = (faultEndTime - System.currentTimeMillis()) / 1000;
            log.warn("[FAULT] {} ainda em estado de falha. Restam {} segundos", requestName, remainingTime);
            applyFaultBehavior(requestName, config);
            return;
        }
        
        if (faultEndTime != null) {
            faultStates.remove(stateKey);
        }
        
        if (random.nextDouble() < config.probability()) {
            log.error("[FAULT] Falha ativada para {}: Type={}, Duration={}s", 
                    requestName, config.type(), config.durationSeconds());
            
            if (config.durationSeconds() > 0) {
                long endTime = System.currentTimeMillis() + (config.durationSeconds() * 1000L);
                faultStates.put(stateKey, endTime);
            }
            
            applyFaultBehavior(requestName, config);
        }
    }
    
    private void applyFaultBehavior(String requestName, FaultConfig config) throws FaultException {
        switch (config.type()) {
            case OMISSION -> throw new OmissionFaultException(requestName);
            case ERROR -> throw new ErrorFaultException(requestName);
            case TIME -> simulateDelay(config.durationSeconds());
            case CRASH -> {
                log.error("[FAULT] CRASH simulado para {}. Encerrando aplicação...", requestName);
                System.exit(1);
            }
        }
    }
    
    private void simulateDelay(int delaySeconds) throws FaultException {
        try {
            log.warn("[FAULT] Simulando atraso de {} segundos", delaySeconds);
            Thread.sleep(delaySeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TimeFaultException("Atraso interrompido", e);
        }
    }
    
    public static class FaultException extends Exception {
        public FaultException(String message) {
            super(message);
        }
        
        public FaultException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class OmissionFaultException extends FaultException {
        public OmissionFaultException(String requestName) {
            super("Falha de omissão em " + requestName);
        }
    }
    
    public static class ErrorFaultException extends FaultException {
        public ErrorFaultException(String requestName) {
            super("Falha de erro em " + requestName);
        }
    }
    
    public static class TimeFaultException extends FaultException {
        public TimeFaultException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}