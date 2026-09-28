package com.ticketmanagement.ticket.service;

public class IllegalTransitionException extends RuntimeException {

  public IllegalTransitionException(String message) {
    super(message);
  }
}
