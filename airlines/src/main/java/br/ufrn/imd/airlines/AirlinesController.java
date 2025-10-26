package br.ufrn.imd.airlines;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.airlines.dto.FlightData;
import br.ufrn.imd.airlines.dto.SellRequest;
import br.ufrn.imd.airlines.dto.SellResponse;

@RestController
public class AirlinesController {

  @GetMapping("/flight")
  public ResponseEntity<FlightData> getFlightInfo(@RequestParam String flight, @RequestParam String day) {
    FlightData response = new FlightData(flight, day, 450.50);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/sell")
  public ResponseEntity<SellResponse> sellTicket(@RequestBody SellRequest sellRequest) {
    String transactionId = UUID.randomUUID().toString();
    SellResponse response = new SellResponse(transactionId);
    return ResponseEntity.ok(response);
  }
}
