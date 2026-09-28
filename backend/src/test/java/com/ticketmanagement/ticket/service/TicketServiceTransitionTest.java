package com.ticketmanagement.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketLifecycle;
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
class TicketServiceTransitionTest {

  @Mock private TicketRepository tickets;
  @Mock private CommentRepository comments;
  private TicketService service;

  @BeforeEach
  void setUp() {
    service =
        new TicketService(
            tickets, comments, new AdjustableClock(Instant.parse("2026-09-29T00:00:00Z")));
    lenient().when(tickets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void everyPairFollowsTheLifecycle() {
    for (TicketStatus from : TicketStatus.values()) {
      for (TicketStatus to : TicketStatus.values()) {
        TicketEntity ticket = openTicket(from);
        when(tickets.findWithCommentsById(1L)).thenReturn(Optional.of(ticket));
        if (TicketLifecycle.isAllowed(from, to)) {
          TicketEntity saved = service.transition(1L, to.name());
          assertThat(saved.getStatus()).isEqualTo(to);
          assertThat(saved.getUpdatedAt()).isAfter(saved.getCreatedAt());
        } else {
          assertThatThrownBy(() -> service.transition(1L, to.name()))
              .isInstanceOf(IllegalTransitionException.class)
              .hasMessage("Cannot change status from " + from.label() + " to " + to.label() + ".");
          assertThat(ticket.getStatus()).isEqualTo(from);
          assertThat(ticket.getTitle()).isEqualTo("Keep");
        }
      }
    }
  }

  private static TicketEntity openTicket(TicketStatus status) {
    TicketEntity ticket = new TicketEntity();
    ticket.setId(1L);
    ticket.setTitle("Keep");
    ticket.setDescription("Body");
    ticket.setPriority(Priority.LOW);
    ticket.setStatus(status);
    ticket.setAssignee("Ada");
    Instant created = Instant.parse("2026-09-29T00:00:00Z");
    ticket.setCreatedAt(created);
    ticket.setUpdatedAt(created);
    return ticket;
  }
}
