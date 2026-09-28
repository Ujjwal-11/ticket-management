package com.ticketmanagement.ticket.service;

public class ClosedTicketException extends RuntimeException {

  public ClosedTicketException() {
    super("Closed tickets cannot be changed. Reopen the ticket first.");
  }
}
