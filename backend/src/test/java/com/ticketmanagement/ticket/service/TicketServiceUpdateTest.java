package com.ticketmanagement.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ticketmanagement.ticket.api.dto.UpdateTicketRequest;
import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.CommentRepository;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import com.ticketmanagement.ticket.persistence.TicketRepository;
import com.ticketmanagement.ticket.support.AdjustableClock;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketServiceUpdateTest {

  @Mock private TicketRepository tickets;
  @Mock private CommentRepository comments;
  private AdjustableClock clock;
  private TicketService service;
  private TicketEntity ticket;

  @BeforeEach
  void setUp() {
    clock = new AdjustableClock(Instant.parse("2026-09-29T00:00:00Z"));
    service = new TicketService(tickets, comments, clock);
    ticket = new TicketEntity();
    ticket.setId(1L);
    ticket.setTitle("Old");
    ticket.setDescription("Body");
    ticket.setPriority(Priority.LOW);
    ticket.setStatus(TicketStatus.OPEN);
    ticket.setAssignee("Ada");
    ticket.setCreatedAt(clock.instant());
    ticket.setUpdatedAt(clock.instant());
    lenient().when(tickets.findWithCommentsById(1L)).thenReturn(Optional.of(ticket));
    lenient().when(tickets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void fieldSaveDoesNotChangeStatusAndClearingAssigneeUnassigns() {
    clock.plusSeconds(5);
    UpdateTicketRequest request = new UpdateTicketRequest();
    request.setTitle("New");
    request.setAssignee("  ");
    TicketEntity saved = service.update(1L, request);
    assertThat(saved.getTitle()).isEqualTo("New");
    assertThat(saved.getAssignee()).isNull();
    assertThat(saved.getStatus()).isEqualTo(TicketStatus.OPEN);
    assertThat(saved.getUpdatedAt()).isAfter(saved.getCreatedAt());
  }

  @Test
  void invalidTitleLeavesPreviousValues() {
    UpdateTicketRequest request = new UpdateTicketRequest();
    request.setTitle(" ");
    assertThatThrownBy(() -> service.update(1L, request))
        .isInstanceOf(TicketValidationException.class);
    assertThat(ticket.getTitle()).isEqualTo("Old");
    verify(tickets, never()).save(any());
  }

  @Test
  void closedTicketRejectsEdits() {
    ticket.setStatus(TicketStatus.CLOSED);
    UpdateTicketRequest request = new UpdateTicketRequest();
    request.setTitle("New");
    assertThatThrownBy(() -> service.update(1L, request))
        .isInstanceOf(ClosedTicketException.class)
        .hasMessage("Closed tickets cannot be changed. Reopen the ticket first.");
    assertThat(ticket.getTitle()).isEqualTo("Old");
  }

  @Test
  void statusOnFieldUpdateIsRejected() {
    UpdateTicketRequest request = new UpdateTicketRequest();
    request.setStatus("CLOSED");
    assertThatThrownBy(() -> service.update(1L, request))
        .isInstanceOf(TicketValidationException.class)
        .extracting(ex -> ((TicketValidationException) ex).errors().getFirst().message())
        .isEqualTo("Status cannot be changed on this request.");
  }
}
