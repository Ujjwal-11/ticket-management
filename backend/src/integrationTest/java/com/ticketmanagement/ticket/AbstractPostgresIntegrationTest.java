package com.ticketmanagement.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractPostgresIntegrationTest {

  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @LocalServerPort protected int port;

  private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();
  private final RestTemplate http = restTemplate();

  private static RestTemplate restTemplate() {
    RestTemplate template = new RestTemplate(new JdkClientHttpRequestFactory());
    template.setErrorHandler(
        new DefaultResponseErrorHandler() {
          @Override
          public boolean hasError(ClientHttpResponse response) {
            return false;
          }
        });
    return template;
  }

  protected JsonNode read(ResponseEntity<String> response) throws Exception {
    if (response.getBody() == null || response.getBody().isBlank()) {
      return null;
    }
    return json.readTree(response.getBody());
  }

  protected ResponseEntity<String> exchange(HttpMethod method, String path, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return http.exchange(url(path), method, new HttpEntity<>(body, headers), String.class);
  }

  protected String url(String path) {
    return "http://localhost:" + port + path;
  }

  @Autowired
  void touchContext() {
    // Ensures the Spring context starts before helpers run.
  }
}
