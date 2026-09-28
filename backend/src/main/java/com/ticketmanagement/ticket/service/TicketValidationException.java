package com.ticketmanagement.ticket.service;

import java.util.List;

public class TicketValidationException extends RuntimeException {

  private final List<FieldViolation> errors;

  public TicketValidationException(List<FieldViolation> errors) {
    super("Validation failed");
    this.errors = List.copyOf(errors);
  }

  public List<FieldViolation> errors() {
    return errors;
  }
}
