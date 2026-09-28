package com.ticketmanagement.ticket.api.dto;

import java.util.List;

public record TicketPageResponse(
    List<TicketSummaryResponse> content, int page, int size, long totalElements, int totalPages) {}
