package br.ufrn.imd.travel.service;

import br.ufrn.imd.travel.dto.BonusRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class BonusQueueService {
    
    private final Queue<BonusRequest> bonusQueue = new ConcurrentLinkedQueue<>();
    private final RestTemplate restTemplate;
    
    @Value("${fidelity.url}")
    private String fidelityURL;
    
    public BonusQueueService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public void addBonusToQueue(BonusRequest bonusRequest) {
        log.info("[Travel] Serviço Fidelity indisponível. Adicionando bônus à fila para usuário: {}", 
                bonusRequest.user());
        bonusQueue.add(bonusRequest);
    }
    
    @Scheduled(fixedDelay = 10000) // refetch in 10 seconds
    public void processBonusQueue() {
        if (bonusQueue.isEmpty()) {
            return;
        }
        
        log.info("[Travel] Processando fila de bônus. Itens na fila: {}", bonusQueue.size());
        
        BonusRequest bonusRequest = bonusQueue.peek();
        
        try {
            log.info("[Travel] Tentando reprocessar bônus para usuário: {}", bonusRequest.user());
            
            restTemplate.postForObject(
                    fidelityURL + "/bonus",
                    bonusRequest,
                    Void.class
            );
            
            bonusQueue.poll();
            log.info("[Travel] Bônus reprocessado com sucesso para usuário: {}", bonusRequest.user());
            
        } catch (Exception e) {
            log.warn("[Travel] Falha ao reprocessar bônus. Fidelity ainda indisponível. " +
                    "Tentando novamente em 10s. Erro: {}", e.getMessage());
        }
    }
    
    public int getQueueSize() {
        return bonusQueue.size();
    }
}