package com.ticketmanagement.ticket.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TicketControllerCreateTest extends TicketWebSlice {

  @Test
  void createReturns201() throws Exception {
    when(tickets.save(any()))
        .thenAnswer(
            invocation -> {
              TicketEntity entity = invocation.getArgument(0);
              entity.setId(4L);
              return entity;
            });
    mockMvc
        .perform(
            post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Printer\",\"description\":\"Jam\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/api/v1/tickets/4"))
        .andExpect(jsonPath("$.status").value(TicketStatus.OPEN.name()))
        .andExpect(jsonPath("$.priority").value(Priority.MEDIUM.name()))
        .andExpect(jsonPath("$.id").value(4));
  }

  @Test
  void blankTitleReturnsProblemDetail() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"  \",\"description\":\"Jam\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.detail").value("Validation failed"))
        .andExpect(jsonPath("$.errors[0].message").value("Title is required"));
  }
}
