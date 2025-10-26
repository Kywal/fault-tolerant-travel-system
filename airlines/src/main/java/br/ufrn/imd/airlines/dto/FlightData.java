package br.ufrn.imd.airlines.dto;

import lombok.Value;

@Value
public class FlightData {
  String flight;
  String day;
  double value;
}
