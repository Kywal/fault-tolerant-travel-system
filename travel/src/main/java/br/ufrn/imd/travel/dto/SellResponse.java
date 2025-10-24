package br.ufrn.imd.travel.dto;

public class SellResponse {
    private String transactionId;

    public SellResponse() {}

    public SellResponse(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
