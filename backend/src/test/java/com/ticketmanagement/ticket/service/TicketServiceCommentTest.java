package com.ticketmanagement.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.CommentEntity;
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
class TicketServiceCommentTest {

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
    ticket.setCreatedAt(clock.instant());
    ticket.setUpdatedAt(clock.instant());
    when(tickets.findWithCommentsById(1L)).thenReturn(Optional.of(ticket));
  }

  @Test
  void storesCommentAndMovesUpdatedAt() {
    when(comments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(tickets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    clock.plusSeconds(3);
    CommentEntity saved = service.addComment(1L, "  Ada  ", "  Noted  ");
    assertThat(saved.getAuthorName()).isEqualTo("Ada");
    assertThat(saved.getBody()).isEqualTo("Noted");
    assertThat(ticket.getUpdatedAt()).isAfter(ticket.getCreatedAt());
  }

  @Test
  void rejectsBlankAndOversizedComments() {
    TicketValidationException ex =
        org.assertj.core.api.Assertions.catchThrowableOfType(
            () -> service.addComment(1L, " ", "c".repeat(2001)), TicketValidationException.class);
    assertThat(ex.errors())
        .extracting(FieldViolation::message)
        .contains("Author name is required", "Comment must be 2,000 characters or fewer");
    verify(comments, never()).save(any());
  }

  @Test
  void blankBodyIsRequired() {
    assertThatThrownBy(() -> service.addComment(1L, "Ada", " "))
        .isInstanceOf(TicketValidationException.class)
        .extracting(ex -> ((TicketValidationException) ex).errors().getFirst().message())
        .isEqualTo("Comment is required");
  }

  @Test
  void closedTicketRejectsComment() {
    ticket.setStatus(TicketStatus.CLOSED);
    assertThatThrownBy(() -> service.addComment(1L, "Ada", "Hi"))
        .isInstanceOf(ClosedTicketException.class)
        .hasMessage("Closed tickets cannot be changed. Reopen the ticket first.");
    verify(comments, never()).save(any());
  }
}
