package com.ticketmanagement.ticket.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TicketControllerUpdateTest extends TicketWebSlice {

  @Test
  void patchKeepsStatus() throws Exception {
    TicketEntity ticket = stored(TicketStatus.OPEN);
    when(tickets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    mockMvc
        .perform(
            patch("/api/v1/tickets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Renamed\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Renamed"))
        .andExpect(jsonPath("$.status").value("OPEN"));
    org.assertj.core.api.Assertions.assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
  }

  @Test
  void blankTitleIsBadRequest() throws Exception {
    stored(TicketStatus.OPEN);
    mockMvc
        .perform(
            patch("/api/v1/tickets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\" \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message").value("Title is required"));
  }

  @Test
  void closedTicketConflicts() throws Exception {
    stored(TicketStatus.CLOSED);
    mockMvc
        .perform(
            patch("/api/v1/tickets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Renamed\"}"))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.detail")
                .value("Closed tickets cannot be changed. Reopen the ticket first."));
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
