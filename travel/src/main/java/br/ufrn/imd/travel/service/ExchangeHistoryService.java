package br.ufrn.imd.travel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;

@Slf4j
@Service
public class ExchangeHistoryService {
    
    private static final int MAX_HISTORY_SIZE = 10;
    private final Queue<Double> rateHistory = new LinkedList<>();
    
    public void addRate(Double rate) {
        if (rate != null && rate > 0) {
            synchronized (rateHistory) {
                rateHistory.offer(rate);
                if (rateHistory.size() > MAX_HISTORY_SIZE) {
                    rateHistory.poll();
                }
                log.debug("[ExchangeHistory] Taxa adicionada: {}. Histórico: {}", rate, rateHistory.size());
            }
        }
    }
    
    public Double getAverageRate() {
        synchronized (rateHistory) {
            if (rateHistory.isEmpty()) {
                log.warn("[ExchangeHistory] Histórico vazio. Não há taxa para calcular média");
                return null;
            }
            
            double sum = rateHistory.stream().mapToDouble(Double::doubleValue).sum();
            double average = sum / rateHistory.size();
            
            log.info("[ExchangeHistory] Média das últimas {} taxas: {}", rateHistory.size(), average);
            return average;
        }
    }
    
    public int getHistorySize() {
        return rateHistory.size();
    }
}