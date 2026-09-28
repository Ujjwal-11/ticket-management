package com.ticketmanagement.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.CommentRepository;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import com.ticketmanagement.ticket.persistence.TicketRepository;
import com.ticketmanagement.ticket.support.AdjustableClock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketServiceCreateTest {

  @Mock private TicketRepository tickets;
  @Mock private CommentRepository comments;

  private TicketService service;

  @BeforeEach
  void setUp() {
    service =
        new TicketService(
            tickets, comments, new AdjustableClock(Instant.parse("2026-09-29T00:00:00Z")));
  }

  @Test
  void createDefaultsPriorityAndStartsOpen() {
    when(tickets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    TicketEntity saved =
        service.create(new CreateTicketRequest("  Printer  ", "  Paper jam  ", null, "  "));
    assertThat(saved.getTitle()).isEqualTo("Printer");
    assertThat(saved.getDescription()).isEqualTo("Paper jam");
    assertThat(saved.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(saved.getStatus()).isEqualTo(TicketStatus.OPEN);
    assertThat(saved.getAssignee()).isNull();
    assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
  }

  @Test
  void blankTitleStoresNothing() {
    assertThatThrownBy(() -> service.create(new CreateTicketRequest("   ", "Broken", "LOW", null)))
        .isInstanceOf(TicketValidationException.class)
        .extracting(ex -> ((TicketValidationException) ex).errors().getFirst().message())
        .isEqualTo("Title is required");
    verify(tickets, never()).save(any());
  }

  @Test
  void rejectsOversizedFields() {
    TicketValidationException ex =
        org.assertj.core.api.Assertions.catchThrowableOfType(
            () ->
                service.create(
                    new CreateTicketRequest(
                        "t".repeat(201), "d".repeat(5001), "NOPE", "a".repeat(101))),
            TicketValidationException.class);
    assertThat(ex.errors())
        .extracting(FieldViolation::message)
        .contains(
            "Title must be 200 characters or fewer",
            "Description must be 5,000 characters or fewer",
            "Priority must be Low, Medium, High, or Urgent",
            "Assignee must be 100 characters or fewer");
    verify(tickets, never()).save(any());
  }
}
