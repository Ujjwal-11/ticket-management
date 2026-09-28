# Implementation Plan: Support Ticket Management System

**Branch**: `001-support-ticket-system` | **Date**: 2026-09-29 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-support-ticket-system/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Team members share one queue. They create tickets, list and open them, edit title, description, priority, and assignee, add comments, search title and description, and filter by status. Data remains after a restart. Status changes only along the spec lifecycle.

Build the Spring Boot API first, with unit tests for the lifecycle and a PostgreSQL integration suite that walks every acceptance scenario. Build the Next.js UI second, through a typed API client.

The planning prompt also listed `CANCELLED` and a shorter chain. That machine is not in the approved spec. This plan follows FR-012. See [research.md](./research.md).

## Technical Context

**Language/Version**: Java 21; TypeScript strict; Node.js 20 LTS

**Primary Dependencies**: Spring Boot 3.5.16, Spring Web, Spring Data JPA, Spring Validation, Flyway, springdoc-openapi 2.8.17; Next.js 16.3.6, React 19

**Storage**: PostgreSQL 16 for the running app and integration tests; H2 in-memory only for `@DataJpaTest`; Flyway migration `V1__create_tickets.sql`

**Testing**: JUnit 5 and Mockito for `TicketLifecycle` (all 16 status pairs) and service rules; `@WebMvcTest`; `@DataJpaTest`; `@SpringBootTest` plus Testcontainers PostgreSQL as `integrationTest`; Vitest and React Testing Library for the UI

**Target Platform**: Linux server for the API; modern desktop browser for the UI

**Project Type**: Web application, backend then frontend

**Performance Goals**: First page of 1,000 tickets visible within 2 seconds (SC-003). A known ticket found and opened within 30 seconds (SC-002).

**Constraints**: Spec messages only for validation, illegal transitions, closed tickets, and missing tickets. Page size fixed at 20. No sign-in, delete, attachments, notifications, or comment editing. Field PATCH cannot change status.

**Scale/Scope**: One internal team. About 1,000 tickets. Two screens plus a create form: queue, ticket detail, create.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Plan | Result |
|------|------|--------|
| I. Controller, service, repository split. UI calls a typed API client only. | `ticket/api`, `ticket/service`, `ticket/persistence`, `ticket/domain`. Frontend `src/services/ticket-api.ts`. | Pass |
| II. Plural REST paths, camelCase JSON, accurate status codes, RFC 7807, OpenAPI. | [contracts/openapi.yaml](./contracts/openapi.yaml) | Pass |
| III. JUnit 5, Mockito, `@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`, Vitest, React Testing Library. Deterministic tests. | Test table below. State-machine tests have no Spring context. | Pass |
| IV. PostgreSQL canonical. H2 only for slice tests. Flyway. `@Transactional` on service methods. Portable SQL. | [data-model.md](./data-model.md) | Pass |
| V. Java 21, TypeScript strict, Spotless, ESLint, Prettier. `./gradlew check` and the npm scripts. | Commands in [quickstart.md](./quickstart.md) | Pass |

No gate fails. Complexity tracking is empty.

### Post-design re-check

Phase 1 artifacts use the same boundaries. The contract has no extra resources. The data model has no extra entities. Integration tests use PostgreSQL, not H2. Still pass.

## Project Structure

### Documentation (this feature)

```text
specs/001-support-ticket-system/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── openapi.yaml
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
backend/
├── build.gradle.kts
├── settings.gradle.kts
├── src/main/java/com/ticketmanagement/
│   ├── TicketManagementApplication.java
│   ├── web/
│   │   ├── CorrelationIdFilter.java
│   │   └── ApiExceptionHandler.java
│   └── ticket/
│       ├── domain/
│       │   ├── TicketStatus.java
│       │   ├── Priority.java
│       │   └── TicketLifecycle.java
│       ├── persistence/
│       │   ├── TicketEntity.java
│       │   ├── CommentEntity.java
│       │   ├── TicketRepository.java
│       │   └── CommentRepository.java
│       ├── service/
│       │   └── TicketService.java
│       └── api/
│           ├── TicketController.java
│           └── dto/
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/V1__create_tickets.sql
├── src/test/java/com/ticketmanagement/ticket/
│   ├── domain/TicketLifecycleTest.java
│   ├── service/TicketServiceTest.java
│   ├── api/TicketControllerTest.java
│   └── persistence/TicketRepositoryTest.java
└── src/integrationTest/java/com/ticketmanagement/ticket/
    └── TicketJourneyIntegrationTest.java

frontend/
├── package.json
├── src/app/
│   ├── page.tsx
│   └── tickets/
│       ├── new/page.tsx
│       └── [id]/page.tsx
├── src/components/
└── src/services/ticket-api.ts

compose.yaml
```

**Structure Decision**: Two projects at the repository root. `backend/` is a Spring Boot service. `frontend/` is a Next.js App Router app. `compose.yaml` runs PostgreSQL 16 for local use. The API is implemented and tested before the UI.

## Delivery sequence

`/speckit-tasks` turns this order into `tasks.md`. Do not implement from this list alone.

1. Gradle project, Java 21, Spotless, Flyway, Compose file for PostgreSQL 16.
2. `TicketLifecycle` and `TicketLifecycleTest` for every status pair.
3. Entities, `V1__create_tickets.sql`, repositories, `@DataJpaTest` for search, filter, and order.
4. `TicketService` with `@Transactional`, Mockito tests for defaults, closed-ticket refusal, and failed saves.
5. Controller, RFC 7807 handler, correlation id, springdoc. `@WebMvcTest` for status codes and spec messages.
6. `integrationTest` on Testcontainers PostgreSQL: create, list, detail, update, every allowed and forbidden transition, comments, search, filter, paging, validation that stores nothing, and a second application context that still sees the same rows.
7. Next.js app, typed client, queue, create, and detail. Status buttons are only the allowed next statuses. Errors show `detail` and field messages from the API.
8. Vitest and React Testing Library for those screens and the client.

## Test map

| Layer | Proves |
|-------|--------|
| `TicketLifecycleTest` | 6 allowed pairs and 10 rejected pairs, including self-transitions |
| `TicketServiceTest` | Create defaults, trim, assignee clear, closed edits, comment rejection, illegal transition leaves the row unchanged |
| `TicketControllerTest` | 201, 200, 400, 404, 409 and the exact spec sentences |
| `TicketRepositoryTest` | Keyword matches title or description, ignores comments, combines with status, page of 20, newest `updatedAt` first |
| `TicketJourneyIntegrationTest` | Every user story against PostgreSQL, including restart |
| Frontend tests | Empty queue, no matches, field errors, closed-ticket message, only legal status actions |

## Out of scope

Sign-in, roles, a user directory, delete, comment edit, attachments, notifications, due dates, priority or assignee filters, comment search, `CANCELLED`, and browser-automation tests.

## Complexity Tracking

No constitution violations.
