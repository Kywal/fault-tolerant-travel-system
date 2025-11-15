package br.ufrn.imd.travel.service;

import br.ufrn.imd.travel.circuit.CircuitBreaker;
import br.ufrn.imd.travel.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
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
    private final ExchangeHistoryService exchangeHistoryService;
    
    // Circuit Breaker para Request 3 (sell flight)
    private final CircuitBreaker sellFlightCircuitBreaker;
    
    private static final int MAX_RETRIES_REQUEST1 = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long SELL_TIMEOUT_MS = 2000;

    public TravelService(RestTemplate restTemplate, 
                        BonusQueueService bonusQueueService,
                        ExchangeHistoryService exchangeHistoryService) {
        this.restTemplate = restTemplate;
        this.bonusQueueService = bonusQueueService;
        this.exchangeHistoryService = exchangeHistoryService;
        
        this.sellFlightCircuitBreaker = new CircuitBreaker(
            "SellFlight", 3, 2, Duration.ofSeconds(30)
        );
    }

    public String processPurchase(BuyTicketRequest request, boolean faultTolerant) {
        FlightData flightData = consultFlight(request, faultTolerant);
        Double valueInReais = calculateValueInReais(flightData, faultTolerant);
        String transactionId = sellFlight(request, faultTolerant);
        registerBonusPoints(request, flightData, faultTolerant);

        log.info("[Travel] Compra finalizada com sucesso!");

        return "Compra realizada! Transaction ID: " + transactionId +
                " | Valor: R$ " + String.format("%.2f", valueInReais);
    }

    private FlightData consultFlight(BuyTicketRequest request, boolean faultTolerant) {
        log.info("[Travel] Passo 1: Consultando voo...");
        
        String flightUrl = airlinesURL + "/flight?flight=" + request.flight() +
                          "&day=" + request.day();
        
        if (!faultTolerant) {
            FlightData flightData = restTemplate.getForObject(flightUrl, FlightData.class);
            log.info("[Travel] Voo encontrado! Valor: ${}", flightData.value());
            return flightData;
        }
        
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < MAX_RETRIES_REQUEST1) {
            attempt++;
            
            try {
                log.info("[Travel] Tentativa {}/{} de consultar voo", attempt, MAX_RETRIES_REQUEST1);
                FlightData flightData = restTemplate.getForObject(flightUrl, FlightData.class);
                log.info("[Travel] Voo encontrado! Valor: ${}", flightData.value());
                return flightData;
                
            } catch (HttpServerErrorException | HttpClientErrorException | ResourceAccessException e) {
                lastException = e;
                log.warn("[Travel] Falha na tentativa {}/{}: {}", attempt, MAX_RETRIES_REQUEST1, e.getMessage());
                
                if (attempt < MAX_RETRIES_REQUEST1) {
                    long delay = RETRY_DELAY_MS * attempt; // Backoff exponencial simples
                    log.info("[Travel] Aguardando {}ms antes da próxima tentativa...", delay);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrompido", ie);
                    }
                }
            }
        }
        
        log.error("[Travel] Todas as {} tentativas falharam para consultar voo", MAX_RETRIES_REQUEST1);
        throw new RuntimeException("Falha ao consultar voo após " + MAX_RETRIES_REQUEST1 + " tentativas", lastException);
    }

    private Double calculateValueInReais(FlightData flightData, boolean faultTolerant) {
        log.info("[Travel] Passo 2: Obtendo taxa de câmbio...");
        
        Double exchangeRate = null;
        
        try {
            exchangeRate = restTemplate.getForObject(exchangeURL + "/convert", Double.class);
            
            if (exchangeRate != null && exchangeRate > 0) {
                log.info("[Travel] Taxa obtida: {}", exchangeRate);
                exchangeHistoryService.addRate(exchangeRate);
            }
            
        } catch (Exception e) {
            log.error("[Travel] Erro ao obter taxa de câmbio: {}", e.getMessage());
            
            if (faultTolerant) {
                exchangeRate = exchangeHistoryService.getAverageRate();
                
                if (exchangeRate == null) {
                    log.error("[Travel] Não há histórico de taxas. Usando taxa padrão de 5.5");
                    exchangeRate = 5.5; // Fallback final
                } else {
                    log.info("[Travel] Usando média do histórico: {}", exchangeRate);
                }
            } else {
                throw new RuntimeException("Falha ao obter taxa de câmbio", e);
            }
        }

        Double valueInReais = flightData.value() * exchangeRate;
        log.info("[Travel] Valor em R$: {}", valueInReais);
        return valueInReais;
    }

    private String sellFlight(BuyTicketRequest request, boolean faultTolerant) {
        log.info("[Travel] Passo 3: Realizando venda...");
        
        Map<String, String> sellRequest = new HashMap<>();
        sellRequest.put("flight", request.flight());
        sellRequest.put("day", request.day());
        
        if (!faultTolerant) {
            Map<String, String> sellResponse = restTemplate.postForObject(
                    airlinesURL + "/sell",
                    sellRequest,
                    Map.class
            );
            String transactionId = sellResponse.get("transactionId");
            log.info("[Travel] Venda realizada! ID: {}", transactionId);
            return transactionId;
        }
        
        try {
            String transactionId = sellFlightCircuitBreaker.execute(() -> {
                long startTime = System.currentTimeMillis();
                
                Map<String, String> sellResponse = restTemplate.postForObject(
                        airlinesURL + "/sell",
                        sellRequest,
                        Map.class
                );
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                
                if (elapsedTime > SELL_TIMEOUT_MS) {
                    log.error("[Travel] Venda excedeu timeout de {}ms (levou {}ms)", 
                            SELL_TIMEOUT_MS, elapsedTime);
                    throw new RuntimeException("Timeout na venda: " + elapsedTime + "ms");
                }
                
                return sellResponse.get("transactionId");
            });
            
            log.info("[Travel] Venda realizada! ID: {}", transactionId);
            return transactionId;
            
        } catch (CircuitBreaker.CircuitBreakerOpenException e) {
            log.error("[Travel] Circuit breaker aberto. Operação bloqueada");
            throw new RuntimeException("Serviço de venda temporariamente indisponível (circuit breaker aberto)", e);
            
        } catch (Exception e) {
            log.error("[Travel] Erro na venda: {}", e.getMessage());
            throw new RuntimeException("Falha ao realizar venda", e);
        }
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