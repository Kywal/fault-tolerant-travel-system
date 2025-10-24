package br.ufrn.imd.travel.dto;

public class BuyTicketResponse {
    private String transactionId;
    private Double valueInReais;
    private String status;

    public BuyTicketResponse() {}

    public BuyTicketResponse(String transactionId, Double valueInReais, String status) {
        this.transactionId = transactionId;
        this.valueInReais = valueInReais;
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Double getValueInReais() {
        return valueInReais;
    }

    public void setValueInReais(Double valueInReais) {
        this.valueInReais = valueInReais;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
