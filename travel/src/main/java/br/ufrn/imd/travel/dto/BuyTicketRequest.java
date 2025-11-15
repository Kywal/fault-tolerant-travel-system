
package br.ufrn.imd.travel.dto;

public record BuyTicketRequest(
    String flight,
    String day,
    String user
) {}