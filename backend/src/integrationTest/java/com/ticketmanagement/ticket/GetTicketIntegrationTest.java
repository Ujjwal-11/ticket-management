package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class GetTicketIntegrationTest extends AbstractPostgresIntegrationTest {

  @Test
  void missingTicketReturnsNotFound() throws Exception {
    var response = exchange(HttpMethod.GET, "/api/v1/tickets/999999", null);
    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(read(response).get("detail").asText()).isEqualTo("Ticket not found");
  }
}
