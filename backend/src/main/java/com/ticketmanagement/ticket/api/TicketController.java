package com.ticketmanagement.ticket.api;

import com.ticketmanagement.ticket.api.dto.CommentResponse;
import com.ticketmanagement.ticket.api.dto.CreateCommentRequest;
import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.api.dto.TicketPageResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.dto.TransitionRequest;
import com.ticketmanagement.ticket.api.dto.UpdateTicketRequest;
import com.ticketmanagement.ticket.persistence.CommentEntity;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import com.ticketmanagement.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "tickets")
public class TicketController {

  private final TicketService tickets;
  private final TicketMapper mapper;

  public TicketController(TicketService tickets, TicketMapper mapper) {
    this.tickets = tickets;
    this.mapper = mapper;
  }

  @GetMapping
  @Operation(summary = "List tickets, newest activity first")
  public TicketPageResponse list(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "page", defaultValue = "0") int page) {
    return mapper.toPage(tickets.list(q, status, page));
  }

  @PostMapping
  @Operation(summary = "Create a ticket")
  public ResponseEntity<TicketResponse> create(@RequestBody CreateTicketRequest request) {
    TicketEntity saved = tickets.create(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(saved.getId())
            .toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(saved));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get one ticket and its comments")
  public TicketResponse get(@PathVariable long id) {
    return mapper.toResponse(tickets.get(id));
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update title, description, priority, or assignee")
  public TicketResponse update(@PathVariable long id, @RequestBody UpdateTicketRequest request) {
    return mapper.toResponse(tickets.update(id, request));
  }

  @PostMapping("/{id}/transitions")
  @Operation(summary = "Move a ticket to an allowed next status")
  public TicketResponse transition(@PathVariable long id, @RequestBody TransitionRequest request) {
    return mapper.toResponse(tickets.transition(id, request.status()));
  }

  @PostMapping("/{id}/comments")
  @Tag(name = "comments")
  @Operation(summary = "Add a comment")
  public ResponseEntity<CommentResponse> comment(
      @PathVariable long id, @RequestBody CreateCommentRequest request) {
    CommentEntity saved = tickets.addComment(id, request.authorName(), request.body());
    return ResponseEntity.status(201).body(mapper.toComment(saved));
  }
}
