package br.ufrn.imd.travel.dto;

public class BuyTicketRequest {
    private String flight;
    private String day;
    private String user;

    public BuyTicketRequest() {}

    public BuyTicketRequest(String flight, String day, String user) {
        this.flight = flight;
        this.day = day;
        this.user = user;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}