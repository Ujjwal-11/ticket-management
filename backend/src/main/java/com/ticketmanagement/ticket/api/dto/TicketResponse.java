package com.ticketmanagement.ticket.api.dto;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import java.time.Instant;
import java.util.List;

public record TicketResponse(
    Long id,
    String title,
    String description,
    Priority priority,
    TicketStatus status,
    String assignee,
    Instant createdAt,
    Instant updatedAt,
    List<CommentResponse> comments) {}
