package br.ufrn.imd.fidelity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class FidelityController {

  @GetMapping("/bonus")
  public ResponseEntity<Void> registerBonus(@RequestParam("user") String userId, @RequestParam("bonus") int bonusValue) {
      System.out.println(
          "[Fidelity] Bônus registrado para usuário: " + userId +
          " | Valor do bônus: " + bonusValue
      );
      return ResponseEntity.ok().build();
  }
}
