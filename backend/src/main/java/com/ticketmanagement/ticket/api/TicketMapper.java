package com.ticketmanagement.ticket.api;

import com.ticketmanagement.ticket.api.dto.CommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketPageResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.dto.TicketSummaryResponse;
import com.ticketmanagement.ticket.persistence.CommentEntity;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import com.ticketmanagement.ticket.service.TicketService;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

  public TicketSummaryResponse toSummary(TicketEntity ticket) {
    return new TicketSummaryResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getPriority(),
        ticket.getStatus(),
        ticket.getAssignee(),
        ticket.getCreatedAt(),
        ticket.getUpdatedAt());
  }

  public TicketResponse toResponse(TicketEntity ticket) {
    List<CommentResponse> comments =
        ticket.getComments().stream()
            .sorted(
                Comparator.comparing(CommentEntity::getCreatedAt)
                    .thenComparing(CommentEntity::getId))
            .map(this::toComment)
            .toList();
    return new TicketResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getDescription(),
        ticket.getPriority(),
        ticket.getStatus(),
        ticket.getAssignee(),
        ticket.getCreatedAt(),
        ticket.getUpdatedAt(),
        comments);
  }

  public CommentResponse toComment(CommentEntity comment) {
    return new CommentResponse(
        comment.getId(), comment.getAuthorName(), comment.getBody(), comment.getCreatedAt());
  }

  public TicketPageResponse toPage(Page<TicketEntity> page) {
    return new TicketPageResponse(
        page.getContent().stream().map(this::toSummary).toList(),
        page.getNumber(),
        TicketService.PAGE_SIZE,
        page.getTotalElements(),
        page.getTotalPages());
  }
}
