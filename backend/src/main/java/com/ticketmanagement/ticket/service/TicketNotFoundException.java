package com.ticketmanagement.ticket.service;

public class TicketNotFoundException extends RuntimeException {

  public TicketNotFoundException() {
    super("Ticket not found");
  }
}
