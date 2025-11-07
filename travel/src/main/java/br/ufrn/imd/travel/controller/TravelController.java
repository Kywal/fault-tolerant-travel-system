package br.ufrn.imd.travel.controller;

import br.ufrn.imd.travel.dto.BuyTicketRequest;
import br.ufrn.imd.travel.service.TravelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class TravelController {
    
    private final TravelService travelService;
    
    public TravelController(TravelService travelService) {
        this.travelService = travelService;
    }
    
    @PostMapping("/buyTicket")
    public ResponseEntity<String> buyTicket(@RequestBody BuyTicketRequest request) {
        log.info("[Travel] Recebido pedido de compra: {} - {}", 
                request.flight(), request.day());
        
        try {
            String result = travelService.processPurchase(request);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("[Travel] Erro ao processar compra: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body("Erro ao processar compra: " + e.getMessage());
        }
    }
}