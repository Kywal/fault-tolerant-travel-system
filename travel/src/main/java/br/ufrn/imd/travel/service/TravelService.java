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

    public TravelService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String processPurchase(BuyTicketRequest request) {
        FlightData flightData = consultFlight(request);
        Double valueInReais = calculateValueInReais(flightData);
        String transactionId = sellFlight(request);
        registerBonusPoints(request, flightData);

        log.info("[Travel] Compra finalizada com sucesso!");

        return "Compra realizada! Transaction ID: " + transactionId +
                " | Valor: R$ " + String.format("%.2f", valueInReais);
    }

    private FlightData consultFlight(BuyTicketRequest request) {
        log.info("[Travel] Passo 1: Consultando voo...");

        String flightUrl = airlinesURL + "/flight?flight=" + request.getFlight() +
                          "&day=" + request.getDay();
        FlightData flightData = restTemplate.getForObject(flightUrl, FlightData.class);

        log.info("[Travel] Voo encontrado! Valor: ${}", flightData.getValue());
        return flightData;
    }

    private Double calculateValueInReais(FlightData flightData) {
        log.info("[Travel] Passo 2: Obtendo taxa de câmbio...");
        Double exchangeRate = restTemplate.getForObject(
                exchangeURL + "/convert",
                Double.class
        );
        log.info("[Travel] Taxa: {}", exchangeRate);

        Double valueInReais = flightData.getValue() * exchangeRate;
        log.info("[Travel] Valor em R$: {}", valueInReais);
        return valueInReais;
    }

    private String sellFlight(BuyTicketRequest request) {
        log.info("[Travel] Passo 3: Realizando venda...");
        Map<String, String> sellRequest = new HashMap<>();
        sellRequest.put("flight", request.getFlight());
        sellRequest.put("day", request.getDay());

        Map<String, String> sellResponse = restTemplate.postForObject(
                airlinesURL + "/sell",
                sellRequest,
                Map.class
        );
        String transactionId = sellResponse.get("transactionId");
        log.info("[Travel] Venda realizada! ID: {}", transactionId);
        return transactionId;
    }

    private void registerBonusPoints(BuyTicketRequest request, FlightData flightData) {
        log.info("[Travel] Passo 4: Registrando pontos...");
        int bonus = (int) Math.round(flightData.getValue());

        BonusRequest bonusRequest = new BonusRequest();
        bonusRequest.setUser(request.getUser());
        bonusRequest.setBonus(bonus);

        restTemplate.postForObject(
                fidelityURL + "/bonus",
                bonusRequest,
                Void.class
        );
    }
}
