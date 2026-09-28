package com.ticketmanagement.ticket.api.dto;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import java.time.Instant;

public record TicketSummaryResponse(
    Long id,
    String title,
    Priority priority,
    TicketStatus status,
    String assignee,
    Instant createdAt,
    Instant updatedAt) {}
