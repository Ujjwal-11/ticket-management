package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class CreateTicketIntegrationTest extends AbstractPostgresIntegrationTest {

  @Test
  void createdTicketCanBeReadAgainAndInvalidCreateStoresNothing() throws Exception {
    String title = "Create-" + System.nanoTime();
    var created =
        exchange(
            HttpMethod.POST,
            "/api/v1/tickets",
            "{\"title\":\"" + title + "\",\"description\":\"Persisted\"}");
    assertThat(created.getStatusCode().value()).isEqualTo(201);
    JsonNode body = read(created);
    long id = body.get("id").asLong();
    var loaded = exchange(HttpMethod.GET, "/api/v1/tickets/" + id, null);
    assertThat(read(loaded).get("title").asText()).isEqualTo(title);
    assertThat(read(loaded).get("status").asText()).isEqualTo("OPEN");

    var rejected =
        exchange(
            HttpMethod.POST, "/api/v1/tickets", "{\"title\":\"   \",\"description\":\"Nope\"}");
    assertThat(rejected.getStatusCode().value()).isEqualTo(400);
    var search = exchange(HttpMethod.GET, "/api/v1/tickets?q=" + title, null);
    assertThat(read(search).get("totalElements").asInt()).isEqualTo(1);
    var unfiltered = exchange(HttpMethod.GET, "/api/v1/tickets", null);
    assertThat(unfiltered.getStatusCode().value()).isEqualTo(200);
    assertThat(read(unfiltered).get("totalElements").asInt()).isGreaterThanOrEqualTo(1);
  }
}
