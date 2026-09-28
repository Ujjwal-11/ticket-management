package com.ticketmanagement.ticket.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.CommentEntity;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TicketControllerCommentTest extends TicketWebSlice {

  @Test
  void addCommentReturns201() throws Exception {
    stored(TicketStatus.OPEN);
    when(comments.save(any()))
        .thenAnswer(
            invocation -> {
              CommentEntity comment = invocation.getArgument(0);
              comment.setId(9L);
              return comment;
            });
    when(tickets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    mockMvc
        .perform(
            post("/api/v1/tickets/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authorName\":\"Ada\",\"body\":\"Noted\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.authorName").value("Ada"))
        .andExpect(jsonPath("$.body").value("Noted"));
  }

  @Test
  void blankCommentIsBadRequest() throws Exception {
    stored(TicketStatus.OPEN);
    mockMvc
        .perform(
            post("/api/v1/tickets/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authorName\":\"Ada\",\"body\":\" \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].message").value("Comment is required"));
  }

  @Test
  void missingTicketIs404() throws Exception {
    when(tickets.findWithCommentsById(44L)).thenReturn(Optional.empty());
    mockMvc
        .perform(
            post("/api/v1/tickets/44/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authorName\":\"Ada\",\"body\":\"Hi\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Ticket not found"));
  }

  @Test
  void closedTicketConflicts() throws Exception {
    stored(TicketStatus.CLOSED);
    mockMvc
        .perform(
            post("/api/v1/tickets/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authorName\":\"Ada\",\"body\":\"Hi\"}"))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.detail")
                .value("Closed tickets cannot be changed. Reopen the ticket first."));
  }

  private void stored(TicketStatus status) {
    TicketEntity ticket = ticket(1L);
    ticket.setTitle("Old");
    ticket.setDescription("Body");
    ticket.setPriority(Priority.LOW);
    ticket.setStatus(status);
    Instant now = Instant.parse("2026-09-29T00:00:00Z");
    ticket.setCreatedAt(now);
    ticket.setUpdatedAt(now);
    when(tickets.findWithCommentsById(1L)).thenReturn(Optional.of(ticket));
  }
}
