package br.ufrn.imd.airlines.fault;

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