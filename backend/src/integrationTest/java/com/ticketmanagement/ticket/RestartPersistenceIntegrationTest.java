package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketmanagement.TicketManagementApplication;
import com.ticketmanagement.ticket.persistence.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;

class RestartPersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

  @Test
  void savedTicketCommentAndRejectedCreateSurviveANewContext() throws Exception {
    String title = "Restart-" + System.nanoTime();
    var created =
        exchange(
            HttpMethod.POST,
            "/api/v1/tickets",
            "{\"title\":\"" + title + "\",\"description\":\"Keep me\",\"priority\":\"HIGH\"}");
    long id = read(created).get("id").asLong();
    exchange(HttpMethod.PATCH, "/api/v1/tickets/" + id, "{\"assignee\":\"Ada\"}");
    exchange(
        HttpMethod.POST,
        "/api/v1/tickets/" + id + "/comments",
        "{\"authorName\":\"Ada\",\"body\":\"Still here\"}");
    exchange(HttpMethod.POST, "/api/v1/tickets", "{\"title\":\"   \",\"description\":\"Dropped\"}");

    try (ConfigurableApplicationContext restarted =
        new SpringApplicationBuilder(TicketManagementApplication.class)
            .run(
                "--server.port=0",
                "--spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                "--spring.datasource.username=" + POSTGRES.getUsername(),
                "--spring.datasource.password=" + POSTGRES.getPassword(),
                "--spring.jpa.hibernate.ddl-auto=validate",
                "--spring.flyway.enabled=true")) {
      TicketRepository repository = restarted.getBean(TicketRepository.class);
      var ticket = repository.findWithCommentsById(id).orElseThrow();
      assertThat(ticket.getTitle()).isEqualTo(title);
      assertThat(ticket.getAssignee()).isEqualTo("Ada");
      assertThat(ticket.getPriority().name()).isEqualTo("HIGH");
      assertThat(ticket.getComments())
          .extracting(comment -> comment.getBody())
          .containsExactly("Still here");
      assertThat(
              repository
                  .search("Dropped", null, org.springframework.data.domain.PageRequest.of(0, 20))
                  .getTotalElements())
          .isZero();
    }
  }
}
