package br.ufrn.imd.airlines;

import br.ufrn.imd.airlines.dto.FlightData;
import br.ufrn.imd.airlines.dto.SellRequest;
import br.ufrn.imd.airlines.dto.SellResponse;
import br.ufrn.imd.airlines.fault.FaultConfig;
import br.ufrn.imd.airlines.fault.FaultSimulator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
public class AirlinesController {

    private final FaultSimulator faultSimulator;

    private static final FaultConfig REQUEST1_FAULT = new FaultConfig(
            FaultConfig.FaultType.OMISSION, 0.2, 0);
    
    private static final FaultConfig REQUEST3_FAULT = new FaultConfig(
            FaultConfig.FaultType.TIME, 0.1, 10);

    public AirlinesController(FaultSimulator faultSimulator) {
        this.faultSimulator = faultSimulator;
    }

    @GetMapping("/flight")
    public ResponseEntity<FlightData> getFlightInfo(
            @RequestParam String flight,
            @RequestParam String day) {
        
        log.info("[Airlines] GET /flight: {} - {}", flight, day);
        
        try {
            faultSimulator.simulateFault("Airlines-GetFlight", REQUEST1_FAULT);

            FlightData response = new FlightData(flight, day, 450.50);
            log.info("[Airlines] Voo retornado: {}", response);
            return ResponseEntity.ok(response);

        } catch (FaultSimulator.OmissionFaultException e) {
            log.error("[Airlines] Omission fault no GET /flight");
            return ResponseEntity.status(503).build();
            
        } catch (Exception e) {
            log.error("[Airlines] Erro inesperado: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<SellResponse> sellTicket(@RequestBody SellRequest sellRequest) {
        
        log.info("[Airlines] POST /sell: {} - {}", 
                sellRequest.flight(), sellRequest.day());
        
        try {
            faultSimulator.simulateFault("Airlines-Sell", REQUEST3_FAULT);

            String transactionId = UUID.randomUUID().toString();
            SellResponse response = new SellResponse(transactionId);
            log.info("[Airlines] Venda concluída: {}", transactionId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[Airlines] Erro no POST /sell: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}