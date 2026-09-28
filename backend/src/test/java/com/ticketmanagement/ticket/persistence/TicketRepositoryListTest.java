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
class TicketRepositoryListTest {

  @Autowired private TicketRepository tickets;

  @Test
  void newestUpdatedThenHighestIdComesFirstAndPageSizeIs20() {
    Instant same = Instant.parse("2026-09-29T00:00:00Z");
    TicketEntity older = save("Older", same);
    TicketEntity lowId = save("Same", same.plusSeconds(10));
    TicketEntity highId = save("Same later id", same.plusSeconds(10));
    highId.setUpdatedAt(same.plusSeconds(10));
    lowId.setUpdatedAt(same.plusSeconds(10));
    tickets.save(lowId);
    tickets.save(highId);
    older.setUpdatedAt(same);
    tickets.save(older);

    var page =
        tickets.search(
            null,
            null,
            PageRequest.of(0, 20, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"))));
    assertThat(page.getContent())
        .extracting(TicketEntity::getId)
        .startsWith(highId.getId(), lowId.getId());
    assertThat(page.getSize()).isEqualTo(20);
  }

  private TicketEntity save(String title, Instant updatedAt) {
    TicketEntity ticket = new TicketEntity();
    ticket.setTitle(title);
    ticket.setDescription("Body");
    ticket.setPriority(Priority.LOW);
    ticket.setStatus(TicketStatus.OPEN);
    ticket.setCreatedAt(updatedAt);
    ticket.setUpdatedAt(updatedAt);
    return tickets.save(ticket);
  }
}
