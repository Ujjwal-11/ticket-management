package com.ticketmanagement.ticket.api;

import com.ticketmanagement.ticket.persistence.CommentRepository;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import com.ticketmanagement.ticket.persistence.TicketRepository;
import com.ticketmanagement.ticket.service.TicketService;
import com.ticketmanagement.web.ApiExceptionHandler;
import com.ticketmanagement.web.ClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TicketController.class)
@Import({TicketService.class, TicketMapper.class, ApiExceptionHandler.class, ClockConfig.class})
public abstract class TicketWebSlice {

  @Autowired protected MockMvc mockMvc;

  @MockitoBean protected TicketRepository tickets;

  @MockitoBean protected CommentRepository comments;

  protected static TicketEntity ticket(long id) {
    TicketEntity entity = new TicketEntity();
    entity.setId(id);
    return entity;
  }
}
