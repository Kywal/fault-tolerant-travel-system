package br.ufrn.imd.travel.controller;

import br.ufrn.imd.travel.dto.BuyTicketRequest;
import br.ufrn.imd.travel.service.TravelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TravelController {
    
    private final TravelService travelService;
    
    public TravelController(TravelService travelService) {
        this.travelService = travelService;
    }
    
    @PostMapping("/buyTicket")
    public ResponseEntity<String> buyTicket(@RequestBody BuyTicketRequest request) {
        System.out.println("[Travel] Recebido pedido de compra: " + 
                          request.getFlight() + " - " + request.getDay());
        
        String result = travelService.processPurchase(request);
        
        return ResponseEntity.ok(result);
    }
}
