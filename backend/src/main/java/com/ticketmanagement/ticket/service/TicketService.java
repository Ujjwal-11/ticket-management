package com.ticketmanagement.ticket.service;

import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.api.dto.UpdateTicketRequest;
import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketLifecycle;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.persistence.CommentEntity;
import com.ticketmanagement.ticket.persistence.CommentRepository;
import com.ticketmanagement.ticket.persistence.TicketEntity;
import com.ticketmanagement.ticket.persistence.TicketRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

  public static final int PAGE_SIZE = 20;

  private final TicketRepository tickets;
  private final CommentRepository comments;
  private final Clock clock;

  public TicketService(TicketRepository tickets, CommentRepository comments, Clock clock) {
    this.tickets = tickets;
    this.comments = comments;
    this.clock = clock;
  }

  @Transactional
  public TicketEntity create(CreateTicketRequest request) {
    List<FieldViolation> errors = new ArrayList<>();
    String title = requiredText(request.title(), "title", "Title", 200, errors);
    String description =
        requiredText(request.description(), "description", "Description", 5000, errors);
    Priority priority = parsePriority(request.priority(), true, errors);
    String assignee = optionalText(request.assignee(), "assignee", "Assignee", 100, errors);
    reject(errors);
    Instant now = clock.instant();
    TicketEntity ticket = new TicketEntity();
    ticket.setTitle(title);
    ticket.setDescription(description);
    ticket.setPriority(priority);
    ticket.setStatus(TicketStatus.OPEN);
    ticket.setAssignee(assignee);
    ticket.setCreatedAt(now);
    ticket.setUpdatedAt(now);
    return tickets.save(ticket);
  }

  @Transactional(readOnly = true)
  public Page<TicketEntity> list(String q, String status, int page) {
    if (page < 0) {
      throw new TicketValidationException(
          List.of(new FieldViolation("page", "Page must be 0 or greater")));
    }
    List<FieldViolation> errors = new ArrayList<>();
    TicketStatus parsedStatus = parseStatus(status, errors);
    reject(errors);
    String keyword = blankToNull(q);
    PageRequest pageable =
        PageRequest.of(
            page, PAGE_SIZE, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id")));
    return tickets.search(keyword, parsedStatus, pageable);
  }

  @Transactional(readOnly = true)
  public TicketEntity get(long id) {
    return tickets.findWithCommentsById(id).orElseThrow(TicketNotFoundException::new);
  }

  @Transactional
  public TicketEntity update(long id, UpdateTicketRequest request) {
    if (request.isStatusPresent()) {
      throw new TicketValidationException(
          List.of(new FieldViolation("status", "Status cannot be changed on this request.")));
    }
    TicketEntity ticket = get(id);
    if (ticket.getStatus() == TicketStatus.CLOSED) {
      throw new ClosedTicketException();
    }
    List<FieldViolation> errors = new ArrayList<>();
    String title = ticket.getTitle();
    String description = ticket.getDescription();
    Priority priority = ticket.getPriority();
    String assignee = ticket.getAssignee();
    if (request.isTitlePresent()) {
      title = requiredText(request.getTitle(), "title", "Title", 200, errors);
    }
    if (request.isDescriptionPresent()) {
      description =
          requiredText(request.getDescription(), "description", "Description", 5000, errors);
    }
    if (request.isPriorityPresent()) {
      priority = parsePriority(request.getPriority(), false, errors);
    }
    if (request.isAssigneePresent()) {
      assignee = optionalText(request.getAssignee(), "assignee", "Assignee", 100, errors);
    }
    reject(errors);
    ticket.setTitle(title);
    ticket.setDescription(description);
    ticket.setPriority(priority);
    ticket.setAssignee(assignee);
    ticket.setUpdatedAt(touch(ticket.getUpdatedAt()));
    return tickets.save(ticket);
  }

  @Transactional
  public TicketEntity transition(long id, String requestedStatus) {
    TicketEntity ticket = get(id);
    List<FieldViolation> errors = new ArrayList<>();
    TicketStatus next = parseRequiredStatus(requestedStatus, errors);
    reject(errors);
    TicketLifecycle.requireTransition(ticket.getStatus(), next);
    ticket.setStatus(next);
    ticket.setUpdatedAt(touch(ticket.getUpdatedAt()));
    return tickets.save(ticket);
  }

  @Transactional
  public CommentEntity addComment(long id, String authorName, String body) {
    TicketEntity ticket = get(id);
    if (ticket.getStatus() == TicketStatus.CLOSED) {
      throw new ClosedTicketException();
    }
    List<FieldViolation> errors = new ArrayList<>();
    String author = requiredText(authorName, "authorName", "Author name", 100, errors);
    String comment = requiredText(body, "body", "Comment", 2000, errors);
    reject(errors);
    Instant now = touch(ticket.getUpdatedAt());
    CommentEntity entity = new CommentEntity();
    entity.setTicket(ticket);
    entity.setAuthorName(author);
    entity.setBody(comment);
    entity.setCreatedAt(now);
    ticket.getComments().add(entity);
    ticket.setUpdatedAt(now);
    CommentEntity saved = comments.save(entity);
    tickets.save(ticket);
    return saved;
  }

  private Instant touch(Instant previous) {
    Instant now = clock.instant();
    if (previous != null && !now.isAfter(previous)) {
      return previous.plusMillis(1);
    }
    return now;
  }

  private static void reject(List<FieldViolation> errors) {
    if (!errors.isEmpty()) {
      throw new TicketValidationException(errors);
    }
  }

  private static String blankToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private static String requiredText(
      String raw, String field, String label, int max, List<FieldViolation> errors) {
    String value = blankToNull(raw);
    if (value == null) {
      errors.add(new FieldViolation(field, label + " is required"));
      return null;
    }
    if (value.length() > max) {
      errors.add(
          new FieldViolation(
              field, label + " must be " + limitLabel(max) + " characters or fewer"));
      return null;
    }
    return value;
  }

  private static String optionalText(
      String raw, String field, String label, int max, List<FieldViolation> errors) {
    String value = blankToNull(raw);
    if (value != null && value.length() > max) {
      errors.add(
          new FieldViolation(
              field, label + " must be " + limitLabel(max) + " characters or fewer"));
      return null;
    }
    return value;
  }

  private static String limitLabel(int max) {
    if (max == 5000) {
      return "5,000";
    }
    if (max == 2000) {
      return "2,000";
    }
    return Integer.toString(max);
  }

  private static Priority parsePriority(
      String raw, boolean defaultMedium, List<FieldViolation> errors) {
    if (raw == null || raw.isBlank()) {
      if (defaultMedium) {
        return Priority.MEDIUM;
      }
      errors.add(new FieldViolation("priority", "Priority must be Low, Medium, High, or Urgent"));
      return null;
    }
    try {
      return Priority.valueOf(raw.trim());
    } catch (IllegalArgumentException ex) {
      errors.add(new FieldViolation("priority", "Priority must be Low, Medium, High, or Urgent"));
      return null;
    }
  }

  private static TicketStatus parseStatus(String raw, List<FieldViolation> errors) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return parseRequiredStatus(raw, errors);
  }

  private static TicketStatus parseRequiredStatus(String raw, List<FieldViolation> errors) {
    if (raw == null || raw.isBlank()) {
      errors.add(new FieldViolation("status", "Status is required"));
      return null;
    }
    try {
      return TicketStatus.valueOf(raw.trim());
    } catch (IllegalArgumentException ex) {
      errors.add(
          new FieldViolation("status", "Status must be Open, In Progress, Resolved, or Closed"));
      return null;
    }
  }
}
