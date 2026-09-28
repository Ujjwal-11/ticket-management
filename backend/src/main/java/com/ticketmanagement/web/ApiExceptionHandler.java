package com.ticketmanagement.web;

import com.ticketmanagement.ticket.service.ClosedTicketException;
import com.ticketmanagement.ticket.service.IllegalTransitionException;
import com.ticketmanagement.ticket.service.TicketNotFoundException;
import com.ticketmanagement.ticket.service.TicketValidationException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(TicketValidationException.class)
  public ResponseEntity<ProblemDetail> validation(
      TicketValidationException exception, HttpServletRequest request) {
    log.warn("Validation failed");
    ProblemDetail problem =
        problem(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed", request);
    problem.setProperty(
        "errors",
        exception.errors().stream()
            .map(error -> Map.of("field", error.field(), "message", error.message()))
            .toList());
    return json(problem);
  }

  @ExceptionHandler(TicketNotFoundException.class)
  public ResponseEntity<ProblemDetail> missing(
      TicketNotFoundException exception, HttpServletRequest request) {
    return json(problem(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage(), request));
  }

  @ExceptionHandler({ClosedTicketException.class, IllegalTransitionException.class})
  public ResponseEntity<ProblemDetail> conflict(
      RuntimeException exception, HttpServletRequest request) {
    log.warn("Ticket change rejected");
    return json(problem(HttpStatus.CONFLICT, "Conflict", exception.getMessage(), request));
  }

  private static ProblemDetail problem(
      HttpStatus status, String title, String detail, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    problem.setType(URI.create("about:blank"));
    problem.setInstance(URI.create(request.getRequestURI()));
    return problem;
  }

  private static ResponseEntity<ProblemDetail> json(ProblemDetail problem) {
    return ResponseEntity.status(problem.getStatus())
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }
}
