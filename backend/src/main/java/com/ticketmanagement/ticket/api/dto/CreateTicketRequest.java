package com.ticketmanagement.ticket.api.dto;

public record CreateTicketRequest(
    String title, String description, String priority, String assignee) {}
