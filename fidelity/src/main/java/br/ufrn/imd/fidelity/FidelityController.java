package br.ufrn.imd.fidelity;

import br.ufrn.imd.fidelity.fault.FaultConfig;
import br.ufrn.imd.fidelity.fault.FaultSimulator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class FidelityController {

    private final FaultSimulator faultSimulator;

    private static final FaultConfig REQUEST4_FAULT = new FaultConfig(
            FaultConfig.FaultType.CRASH, 0.02, 0);

    public FidelityController(FaultSimulator faultSimulator) {
        this.faultSimulator = faultSimulator;
    }

    @PostMapping("/bonus")
    public ResponseEntity<Void> registerBonus(@RequestBody BonusRequest request) {
        
        log.info("[Fidelity] POST /bonus: user={}, bonus={}", 
                request.user(), request.bonus());
        
        try {
            faultSimulator.simulateFault("Fidelity-Bonus", REQUEST4_FAULT);

            log.info("[Fidelity] Bônus registrado com sucesso!");
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("[Fidelity] Erro no POST /bonus: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    public record BonusRequest(String user, int bonus) { }
}