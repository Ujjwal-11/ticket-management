package com.ticketmanagement.ticket.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.ticket.domain.TicketStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class TicketControllerSearchTest extends TicketWebSlice {

  @Test
  void passesKeywordAndStatusToTheQuery() throws Exception {
    when(tickets.search(nullable(String.class), nullable(TicketStatus.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));
    mockMvc
        .perform(get("/api/v1/tickets").param("q", "  login ").param("status", "OPEN"))
        .andExpect(status().isOk());
    verify(tickets).search(eq("login"), eq(TicketStatus.OPEN), any(Pageable.class));
  }
}
