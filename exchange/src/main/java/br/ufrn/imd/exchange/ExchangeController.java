package br.ufrn.imd.exchange;

import br.ufrn.imd.exchange.fault.FaultConfig;
import br.ufrn.imd.exchange.fault.FaultSimulator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@Slf4j
@RestController
public class ExchangeController {

    private final FaultSimulator faultSimulator;
    private final Random random = new Random();

    private static final FaultConfig REQUEST2_FAULT = new FaultConfig(
            FaultConfig.FaultType.ERROR, 0.1, 5);

    public ExchangeController(FaultSimulator faultSimulator) {
        this.faultSimulator = faultSimulator;
    }

    @GetMapping("/convert")
    public ResponseEntity<Double> getExchangeRate() {
        log.info("[Exchange] GET /convert");
        
        try {
            faultSimulator.simulateFault("Exchange-Convert", REQUEST2_FAULT);
            
            double rate = 5.0 + random.nextDouble();
            log.info("[Exchange] Taxa retornada: {}", rate);
            return ResponseEntity.ok(rate);
            
        } catch (FaultSimulator.ErrorFaultException e) {
            log.error("[Exchange] Error fault no GET /convert");
            return ResponseEntity.status(500).body(null);
            
        } catch (Exception e) {
            log.error("[Exchange] Erro inesperado: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}