package br.ufrn.imd.airlines.dto;

import lombok.Value;

@Value
public class SellRequest {
  String flight;
  String day;
}
