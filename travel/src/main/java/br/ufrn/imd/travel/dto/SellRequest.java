package br.ufrn.imd.travel.dto;

public class SellRequest {
    private String flight;
    private String day;

    public SellRequest() {}

    public SellRequest(String flight, String day) {
        this.flight = flight;
        this.day = day;
    }

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flight) {
        this.flight = flight;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}