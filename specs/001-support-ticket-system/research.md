# Research: Support Ticket Management System

## Lifecycle

**Decision**: Implement the approved spec lifecycle only.

| From | Allowed next |
|------|----------------|
| Open | In Progress |
| In Progress | Resolved, Open |
| Resolved | Closed, In Progress |
| Closed | In Progress |

Every other pair, including a status changed to itself, is rejected. The stored status stays unchanged.

**Rationale**: `specs/001-support-ticket-system/spec.md` FR-012 and User Story 5 are the approved behavior. The planning prompt also says not to add features outside the spec. `CANCELLED` is not in the spec. Send-back (`In Progress` → `Open`) and reopen (`Resolved` → `In Progress`, `Closed` → `In Progress`) are in the spec.

**Alternatives considered**: The planning prompt listed `OPEN → IN_PROGRESS → RESOLVED → CLOSED`, `OPEN → CANCELLED`, and `IN_PROGRESS → CANCELLED`. That machine adds a status and drops three required transitions. It was rejected so acceptance tests stay aligned with the spec. The listed chain without `CANCELLED` is the happy path already inside FR-012.

## Stack versions

**Decision**: Java 21, Spring Boot 3.5.16, springdoc-openapi-starter-webmvc-ui 2.8.17, Next.js 16.3.6, React 19, Node.js 20 LTS, PostgreSQL 16, Gradle Kotlin DSL.

**Rationale**: The constitution locks Java 21, Spring Boot 3.x, PostgreSQL 16+, and Next.js 14+ with the App Router. Spring Boot 4.1.1 is current but outside that lock. Spring Boot 3.5.16 is the newest 3.5 release. springdoc 2.8.17 is the Boot 3 line; springdoc 3.x targets Boot 4. Next.js 14 is out of security support. Next.js 16.3.6 is the active LTS and still uses the App Router and React 19, which the constitution allows.

**Alternatives considered**: Spring Boot 4.1.1, springdoc 3.0.3, Next.js 14.2.35, Next.js 15.5.26. Boot 4 breaks the constitution. Next.js 14 is unsupported. Next.js 15.5 is maintenance-only and nearing end of support.

## Persistence

**Decision**: Flyway migrations in `backend/src/main/resources/db/migration`. PostgreSQL 16 for local run and for `@SpringBootTest` via Testcontainers. H2 in-memory only for `@DataJpaTest`. Queries use `lower(column) like lower(concat('%', :q, '%'))`, not vendor-only operators.

**Rationale**: The constitution requires versioned migrations, PostgreSQL as the canonical engine, and H2 only for fast slice tests. A portable predicate keeps slice tests and integration tests on the same search rule.

**Alternatives considered**: Hibernate `ddl-auto`, `ILIKE`, and H2 for integration tests. Schema auto-update is forbidden. `ILIKE` is a dialect split. H2 integration tests would not prove the restart and migration requirements.

## API shape

**Decision**: JSON under `/api/v1`. Field edits and status changes are separate operations. List rows omit description and comments. Ticket detail includes comments, oldest first. Errors are RFC 7807. User-facing sentences from the spec are copied into `detail` and `errors[].message`.

**Rationale**: The spec says a field save must not change status. One PATCH that accepts both would make that rule easy to break. The queue does not display description or comments. Problem Details is a constitution rule, and the spec fixes the sentences the member must see.

**Alternatives considered**: One PATCH that includes `status`, and a separate comments collection on the detail page. A combined PATCH needs extra ignore rules. A second round trip is unnecessary because the detail response already returns the comments.

## Status controls in the UI

**Decision**: The ticket screen shows only the next statuses allowed from the current one. The API still rejects every other requested status with the spec sentence.

**Rationale**: Members should not be offered illegal moves. User Story 5 still requires a rejected illegal request. Integration tests send those requests directly.

**Alternatives considered**: A dropdown of all four statuses. That makes the error common in normal use and is not required by the spec.

## Tests

**Decision**: Backend first. Unit tests cover `TicketLifecycle` for all 16 status pairs. Service unit tests cover closed-ticket rules and failed saves. `@WebMvcTest` and `@DataJpaTest` satisfy the constitution. `integrationTest` is `@SpringBootTest` plus Testcontainers PostgreSQL and walks every acceptance scenario, including a new application context against the same database. Frontend tests use Vitest and React Testing Library against the typed API client. No browser-automation dependency.

**Rationale**: The constitution names the test layers and the `integrationTest` task. The planning prompt asks for state-machine coverage and full end-to-end integration. An HTTP call through Spring into PostgreSQL is that end-to-end path. Playwright would be a new dependency and is not required to prove the spec.

**Alternatives considered**: Browser end-to-end tests, and integration tests on H2. Browser tests duplicate the API suite. H2 does not satisfy the constitution's integration engine.

## Small behavior defaults

**Decision**: Ticket number is the numeric `id`. Timestamps are UTC instants in the API and shown in the viewer's local time, to the minute. Equal `updatedAt` sorts by `id` descending. Equal comment times sort by `id` ascending. Queue page size is always 20. Page index is zero-based in the API. Blank search text means no keyword. `null` or blank assignee clears the assignee. There is no row version; the later save wins.

**Rationale**: The spec leaves these unspecified and they do not change ticket behavior. Fixing them here keeps the contract and the tests from drifting.

**Alternatives considered**: A separate display code, optimistic locking, and client-chosen page size. None of those are in the spec.
