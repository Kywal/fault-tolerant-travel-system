package br.ufrn.imd.travel.dto;

public class FlightData {
    private String flight;
    private String day;
    private Double value;
    
    public String getFlight() { return flight; }
    public void setFlight(String flight) { this.flight = flight; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
