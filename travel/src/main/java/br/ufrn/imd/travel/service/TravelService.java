package br.ufrn.imd.travel.service;

import br.ufrn.imd.travel.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TravelService {

    @Value("${airlines.url}")
    private String airlinesURL;

    @Value("${exchange.url}")
    private String exchangeURL;

    @Value("${fidelity.url}")
    private String fidelityURL;

    private final RestTemplate restTemplate;

    private final BonusQueueService bonusQueueService;

    public TravelService(RestTemplate restTemplate, BonusQueueService bonusQueueService) {
        this.restTemplate = restTemplate;
        this.bonusQueueService = bonusQueueService;
    }

    public String processPurchase(BuyTicketRequest request, boolean faultTolerant) {
        FlightData flightData = consultFlight(request);
        Double valueInReais = calculateValueInReais(flightData);
        String transactionId = sellFlight(request);
        registerBonusPoints(request, flightData, faultTolerant);

        log.info("[Travel] Compra finalizada com sucesso!");

        return "Compra realizada! Transaction ID: " + transactionId +
                " | Valor: R$ " + String.format("%.2f", valueInReais);
    }

    private FlightData consultFlight(BuyTicketRequest request) {
        log.info("[Travel] Passo 1: Consultando voo...");
        
        String flightUrl = airlinesURL + "/flight?flight=" + request.flight() +
                          "&day=" + request.day();
        FlightData flightData = restTemplate.getForObject(flightUrl, FlightData.class);

        log.info("[Travel] Voo encontrado! Valor: ${}", flightData.value());
        return flightData;
    }

    private Double calculateValueInReais(FlightData flightData) {
        log.info("[Travel] Passo 2: Obtendo taxa de câmbio...");
        
        Double exchangeRate = restTemplate.getForObject(
                exchangeURL + "/convert",
                Double.class
        );
        log.info("[Travel] Taxa: {}", exchangeRate);

        Double valueInReais = flightData.value() * exchangeRate;
        log.info("[Travel] Valor em R$: {}", valueInReais);
        return valueInReais;
    }

    private String sellFlight(BuyTicketRequest request) {
        log.info("[Travel] Passo 3: Realizando venda...");
        
        Map<String, String> sellRequest = new HashMap<>();
        sellRequest.put("flight", request.flight());
        sellRequest.put("day", request.day());

        Map<String, String> sellResponse = restTemplate.postForObject(
                airlinesURL + "/sell",
                sellRequest,
                Map.class
        );
        String transactionId = sellResponse.get("transactionId");
        log.info("[Travel] Venda realizada! ID: {}", transactionId);
        return transactionId;
    }

     private void registerBonusPoints(BuyTicketRequest request, FlightData flightData, boolean faultTolerant) {
        log.info("[Travel] Passo 4: Registrando pontos...");
        
        int bonus = (int) Math.round(flightData.value());
        BonusRequest bonusRequest = new BonusRequest(request.user(), bonus);

        try {
            restTemplate.postForObject(
                    fidelityURL + "/bonus",
                    bonusRequest,
                    Void.class
            );
            
            log.info("[Travel] Pontos registrados com sucesso!");
            
        } catch (Exception e) {
            log.error("[Travel] Falha ao registrar bônus no Fidelity: {}", e.getMessage());
            
            if (faultTolerant) {
                log.info("[Travel] Tolerância a falhas ATIVA - Adicionando bônus à fila");
                bonusQueueService.addBonusToQueue(bonusRequest);
            } else {
                throw new RuntimeException("Falha ao registrar bônus (tolerância a falhas desabilitada)", e);
            }
        }
    }
}