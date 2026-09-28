package com.ticketmanagement.ticket.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.CommentEntity;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TicketControllerGetTest extends TicketWebSlice {

  @Test
  void returnsCommentsOldestFirst() throws Exception {
    TicketEntity ticket = ticket(8L);
    ticket.setTitle("Detail");
    ticket.setDescription("Full");
    ticket.setPriority(Priority.LOW);
    ticket.setStatus(TicketStatus.OPEN);
    ticket.setCreatedAt(Instant.parse("2026-09-29T00:00:00Z"));
    ticket.setUpdatedAt(Instant.parse("2026-09-29T00:00:00Z"));
    ticket.getComments().add(comment(2L, "Later", Instant.parse("2026-09-29T00:02:00Z"), ticket));
    ticket.getComments().add(comment(1L, "Earlier", Instant.parse("2026-09-29T00:01:00Z"), ticket));
    when(tickets.findWithCommentsById(8L)).thenReturn(Optional.of(ticket));
    mockMvc
        .perform(get("/api/v1/tickets/8"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("Full"))
        .andExpect(jsonPath("$.comments[0].body").value("Earlier"))
        .andExpect(jsonPath("$.comments[1].body").value("Later"));
  }

  @Test
  void missingTicket() throws Exception {
    when(tickets.findWithCommentsById(99L)).thenReturn(Optional.empty());
    mockMvc
        .perform(get("/api/v1/tickets/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Ticket not found"));
  }

  private static CommentEntity comment(
      long id, String body, Instant createdAt, TicketEntity ticket) {
    CommentEntity comment = new CommentEntity();
    comment.setId(id);
    comment.setBody(body);
    comment.setAuthorName("Ada");
    comment.setCreatedAt(createdAt);
    comment.setTicket(ticket);
    return comment;
  }
}
