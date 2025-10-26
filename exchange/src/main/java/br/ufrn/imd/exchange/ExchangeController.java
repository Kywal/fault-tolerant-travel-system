package br.ufrn.imd.exchange;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExchangeController {

  @GetMapping("/convert")
    public ResponseEntity<Double> getConversionRate() {

        double rate = ThreadLocalRandom.current().nextDouble(5.0, 6.0);
        double roundedRate = Math.round(rate * 100.0) / 100.0;
        return ResponseEntity.ok(roundedRate);
    }
}
