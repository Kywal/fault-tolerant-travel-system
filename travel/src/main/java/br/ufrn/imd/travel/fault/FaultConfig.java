package br.ufrn.imd.travel.fault;

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