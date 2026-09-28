package com.ticketmanagement.ticket.api.dto;

import java.time.Instant;

public record CommentResponse(Long id, String authorName, String body, Instant createdAt) {}
