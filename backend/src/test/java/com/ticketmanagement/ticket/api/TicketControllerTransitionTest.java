package com.ticketmanagement.ticket.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TicketControllerTransitionTest extends TicketWebSlice {

  @Test
  void illegalTransitionNamesCurrentAndRequestedStatus() throws Exception {
    TicketEntity ticket = stored(TicketStatus.OPEN);
    mockMvc
        .perform(
            post("/api/v1/tickets/1/transitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CLOSED\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Cannot change status from Open to Closed."));
    org.assertj.core.api.Assertions.assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
  }

  @Test
  void allowedTransitionUpdatesStatus() throws Exception {
    stored(TicketStatus.OPEN);
    when(tickets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    mockMvc
        .perform(
            post("/api/v1/tickets/1/transitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_PROGRESS\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
  }

  private TicketEntity stored(TicketStatus status) {
    TicketEntity ticket = ticket(1L);
    ticket.setTitle("Old");
    ticket.setDescription("Body");
    ticket.setPriority(Priority.LOW);
    ticket.setStatus(status);
    Instant now = Instant.parse("2026-09-29T00:00:00Z");
    ticket.setCreatedAt(now);
    ticket.setUpdatedAt(now);
    when(tickets.findWithCommentsById(1L)).thenReturn(Optional.of(ticket));
    return ticket;
  }
}
