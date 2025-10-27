package br.ufrn.imd.fidelity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class FidelityController {

  @PostMapping("/bonus")
public ResponseEntity<Void> registerBonus(@RequestBody BonusRequest request) {
    System.out.println(
        "[Fidelity] Bônus registrado para usuário: " + request.getUser() +
        " | Valor do bônus: " + request.getBonus()
    );
    return ResponseEntity.ok().build();
}

  public static class BonusRequest {
        private String user;
        private int bonus;
        
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        public int getBonus() { return bonus; }
        public void setBonus(int bonus) { this.bonus = bonus; }
    }
}
