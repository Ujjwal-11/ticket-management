package com.ticketmanagement.ticket.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketmanagement.ticket.domain.Priority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
class TicketRepositorySearchTest {

  @Autowired private TicketRepository tickets;
  @Autowired private CommentRepository comments;

  @Test
  void keywordMatchesTitleOrDescriptionAndCombinesWithStatus() {
    save("Alpha Login", "nothing", TicketStatus.OPEN);
    save("Other", "Password RESET steps", TicketStatus.RESOLVED);
    save("Skip", "login hidden", TicketStatus.CLOSED);
    TicketEntity withComment = save("Plain", "no match", TicketStatus.OPEN);
    CommentEntity comment = new CommentEntity();
    comment.setTicket(withComment);
    comment.setAuthorName("Ada");
    comment.setBody("secret keyword");
    comment.setCreatedAt(Instant.parse("2026-09-29T00:00:00Z"));
    comments.save(comment);

    var sort = PageRequest.of(0, 20, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id")));
    assertThat(tickets.search("login", null, sort).getContent())
        .extracting(TicketEntity::getTitle)
        .contains("Alpha Login")
        .doesNotContain("Plain");
    assertThat(tickets.search("reset", TicketStatus.RESOLVED, sort).getContent())
        .extracting(TicketEntity::getTitle)
        .containsExactly("Other");
    assertThat(tickets.search("secret", null, sort).getContent()).isEmpty();
    assertThat(tickets.search(null, null, sort).getTotalElements()).isEqualTo(4);
  }

  private TicketEntity save(String title, String description, TicketStatus status) {
    TicketEntity ticket = new TicketEntity();
    ticket.setTitle(title);
    ticket.setDescription(description);
    ticket.setPriority(Priority.LOW);
    ticket.setStatus(status);
    Instant now = Instant.parse("2026-09-29T00:00:00Z");
    ticket.setCreatedAt(now);
    ticket.setUpdatedAt(now);
    return tickets.saveAndFlush(ticket);
  }
}
