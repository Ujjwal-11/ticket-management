package com.ticketmanagement.ticket.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class TicketControllerListTest extends TicketWebSlice {

  @Test
  void listOmitsDescriptionAndFixesPageSize() throws Exception {
    TicketEntity ticket = ticket(3L);
    ticket.setTitle("Queue");
    ticket.setDescription("Hidden");
    ticket.setPriority(Priority.HIGH);
    ticket.setStatus(TicketStatus.OPEN);
    ticket.setCreatedAt(Instant.parse("2026-09-29T00:00:00Z"));
    ticket.setUpdatedAt(Instant.parse("2026-09-29T00:00:00Z"));
    when(tickets.search(nullable(String.class), nullable(TicketStatus.class), any()))
        .thenReturn(new PageImpl<>(List.of(ticket), PageRequest.of(0, 20), 1));
    mockMvc
        .perform(get("/api/v1/tickets"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.content[0].title").value("Queue"))
        .andExpect(jsonPath("$.content[0].description").doesNotExist())
        .andExpect(jsonPath("$.content[0].comments").doesNotExist());
  }
}
