package br.ufrn.imd.fidelity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class FidelityController {

    @PostMapping("/bonus")
    public ResponseEntity<Void> registerBonus(@RequestBody BonusRequest request) {
        log.info("[Fidelity] Bônus registrado para usuário: {} | Valor do bônus: {}", request.user(), request.bonus());
        return ResponseEntity.ok().build();
    }

    public record BonusRequest(String user, int bonus) { }

}
