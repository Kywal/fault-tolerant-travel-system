package br.ufrn.imd.exchange.fault;

public record FaultConfig(
        FaultType type,
        double probability,
        int durationSeconds
) {
    public enum FaultType {
        OMISSION,
        ERROR,
        TIME,
        CRASH
    }
}