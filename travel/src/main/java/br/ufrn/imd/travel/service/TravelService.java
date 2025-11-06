package br.ufrn.imd.travel.service;

import br.ufrn.imd.travel.dto.*;
import br.ufrn.imd.travel.fault.FaultConfig;
import br.ufrn.imd.travel.fault.FaultSimulator;
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
    private final FaultSimulator faultSimulator;

    private static final FaultConfig REQUEST1_FAULT = new FaultConfig(
            FaultConfig.FaultType.OMISSION, 0.2, 0);
    
    private static final FaultConfig REQUEST2_FAULT = new FaultConfig(
            FaultConfig.FaultType.ERROR, 0.1, 5);
    
    private static final FaultConfig REQUEST3_FAULT = new FaultConfig(
            FaultConfig.FaultType.TIME, 0.1, 10);
    
    private static final FaultConfig REQUEST4_FAULT = new FaultConfig(
            FaultConfig.FaultType.CRASH, 0.02, 0);

    public TravelService(RestTemplate restTemplate, FaultSimulator faultSimulator) {
        this.restTemplate = restTemplate;
        this.faultSimulator = faultSimulator;
    }

    public String processPurchase(BuyTicketRequest request) {
        try {
            FlightData flightData = consultFlight(request);
            Double valueInReais = calculateValueInReais(flightData);
            String transactionId = sellFlight(request);
            registerBonusPoints(request, flightData);

            log.info("[Travel] Compra finalizada com sucesso!");

            return "Compra realizada! Transaction ID: " + transactionId +
                    " | Valor: R$ " + String.format("%.2f", valueInReais);
                    
        } catch (FaultSimulator.OmissionFaultException e) {
            log.error("[Travel] Falha de omissão detectada: {}", e.getMessage());
            throw new RuntimeException("Serviço temporariamente indisponível (Omission)", e);
            
        } catch (FaultSimulator.ErrorFaultException e) {
            log.error("[Travel] Falha de erro detectada: {}", e.getMessage());
            throw new RuntimeException("Erro no processamento da requisição (Error)", e);
            
        } catch (FaultSimulator.TimeFaultException e) {
            log.error("[Travel] Falha de tempo detectada: {}", e.getMessage());
            throw new RuntimeException("Timeout na operação (Time)", e);
            
        } catch (Exception e) {
            log.error("[Travel] Erro inesperado: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar compra: " + e.getMessage(), e);
        }
    }

    private FlightData consultFlight(BuyTicketRequest request) throws FaultSimulator.FaultException {
        log.info("[Travel] Passo 1: Consultando voo...");
        
        faultSimulator.simulateFault("Request1", REQUEST1_FAULT);

        String flightUrl = airlinesURL + "/flight?flight=" + request.flight() +
                          "&day=" + request.day();
        FlightData flightData = restTemplate.getForObject(flightUrl, FlightData.class);

        log.info("[Travel] Voo encontrado! Valor: ${}", flightData.value());
        return flightData;
    }

    private Double calculateValueInReais(FlightData flightData) throws FaultSimulator.FaultException {
        log.info("[Travel] Passo 2: Obtendo taxa de câmbio...");
        
        faultSimulator.simulateFault("Request2", REQUEST2_FAULT);
        
        Double exchangeRate = restTemplate.getForObject(
                exchangeURL + "/convert",
                Double.class
        );
        log.info("[Travel] Taxa: {}", exchangeRate);

        Double valueInReais = flightData.value() * exchangeRate;
        log.info("[Travel] Valor em R$: {}", valueInReais);
        return valueInReais;
    }

    private String sellFlight(BuyTicketRequest request) throws FaultSimulator.FaultException {
        log.info("[Travel] Passo 3: Realizando venda...");
        
        faultSimulator.simulateFault("Request3", REQUEST3_FAULT);
        
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

    private void registerBonusPoints(BuyTicketRequest request, FlightData flightData) throws FaultSimulator.FaultException {
        log.info("[Travel] Passo 4: Registrando pontos...");
        
        faultSimulator.simulateFault("Request4", REQUEST4_FAULT);
        
        int bonus = (int) Math.round(flightData.value());
        BonusRequest bonusRequest = new BonusRequest(request.user(), bonus);

        restTemplate.postForObject(
                fidelityURL + "/bonus",
                bonusRequest,
                Void.class
        );
        
        log.info("[Travel] Pontos registrados com sucesso!");
    }
}