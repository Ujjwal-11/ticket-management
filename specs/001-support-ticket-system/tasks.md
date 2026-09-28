---

description: "Task list template for feature implementation"
---

# Tasks: Support Ticket Management System

**Input**: Design documents from `/specs/001-support-ticket-system/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Included. The plan and the task request require backend unit tests, state-machine coverage, and PostgreSQL integration tests before UI work.

**Organization**: Backend stories run first, in spec order. Frontend stories start only after the API stories. Scope is FR-001 through FR-020. No `CANCELLED` status, sign-in, delete, attachments, or notifications.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Web app**: `backend/src/`, `frontend/src/`
- Integration tests: `backend/src/integrationTest/java/`
- Unit, web-slice, and data-slice tests: `backend/src/test/java/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create `compose.yaml` with a PostgreSQL 16 service, and create the `backend/` and `frontend/` directories
- [X] T002 Create the Java 21 Spring Boot 3.5.16 Gradle project in `backend/settings.gradle.kts` and `backend/build.gradle.kts` with spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, flyway, postgresql, h2, springdoc-openapi-starter-webmvc-ui 2.8.17, spring-boot-starter-test, Testcontainers PostgreSQL, Spotless with google-java-format, an `integrationTest` source set, and `check` depending on `integrationTest`
- [X] T003 [P] Add the Gradle wrapper in `backend/gradlew` and `backend/gradle/wrapper/gradle-wrapper.properties`
- [X] T004 [P] Scaffold the Next.js 16.3.6 and React 19 app with TypeScript `strict` true, ESLint, Prettier, and scripts `lint`, `type-check`, `test`, and `build` in `frontend/package.json`, `frontend/tsconfig.json`, `frontend/eslint.config.mjs`, and `frontend/.prettierrc`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T005 Create the application entry in `backend/src/main/java/com/ticketmanagement/TicketManagementApplication.java`
- [X] T006 Configure the default PostgreSQL datasource, the `test` H2 in-memory datasource, `ddl-auto: validate`, and Flyway in `backend/src/main/resources/application.yml`
- [X] T007 [P] Write failing tests for all 16 status pairs, including a status changed to itself, in `backend/src/test/java/com/ticketmanagement/ticket/domain/TicketLifecycleTest.java`
- [X] T008 Implement `OPEN`, `IN_PROGRESS`, `RESOLVED`, and `CLOSED` plus `LOW`, `MEDIUM`, `HIGH`, and `URGENT` in `backend/src/main/java/com/ticketmanagement/ticket/domain/TicketStatus.java` and `backend/src/main/java/com/ticketmanagement/ticket/domain/Priority.java`, and implement `backend/src/main/java/com/ticketmanagement/ticket/domain/TicketLifecycle.java` allowing only OPEN→IN_PROGRESS, IN_PROGRESS→RESOLVED, IN_PROGRESS→OPEN, RESOLVED→CLOSED, RESOLVED→IN_PROGRESS, and CLOSED→IN_PROGRESS
- [X] T009 [P] Create `backend/src/main/java/com/ticketmanagement/ticket/persistence/TicketEntity.java` with generated `id`, `title` varchar(200) not null, `description` varchar(5000) not null, `priority` varchar(16) not null, `status` varchar(16) not null, `assignee` varchar(100) null, `createdAt` timestamptz not null, and `updatedAt` timestamptz not null
- [X] T010 [P] Create `backend/src/main/java/com/ticketmanagement/ticket/persistence/CommentEntity.java` with generated `id`, required `ticket` reference, `authorName` varchar(100) not null, `body` varchar(2000) not null, and `createdAt` timestamptz not null
- [X] T011 Create `backend/src/main/resources/db/migration/V1__create_tickets.sql` with the `tickets` and `comments` tables, index `(updated_at desc, id desc)`, and index `(ticket_id, created_at, id)`
- [X] T012 Create `backend/src/main/java/com/ticketmanagement/ticket/persistence/TicketRepository.java` and `backend/src/main/java/com/ticketmanagement/ticket/persistence/CommentRepository.java`
- [X] T013 Create the Testcontainers PostgreSQL 16 base in `backend/src/integrationTest/java/com/ticketmanagement/ticket/AbstractPostgresIntegrationTest.java`
- [X] T014 Implement RFC 7807 responses with `type`, `title`, `status`, `detail`, `instance`, and optional `errors[].field` plus `errors[].message` in `backend/src/main/java/com/ticketmanagement/web/ApiExceptionHandler.java`
- [X] T015 Read or generate `X-Correlation-Id`, store it in the logging context, and return it on the response in `backend/src/main/java/com/ticketmanagement/web/CorrelationIdFilter.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Create a ticket (Priority: P1) 🎯 MVP

**Goal**: A team member submits a title, description, optional assignee, and priority, and a new Open ticket is stored.

**Independent Test**: Submit a valid request and confirm a new Open ticket appears with the entered title, description, and priority. A blank title creates nothing and returns `Title is required`.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T016 [P] [US1] Write failing service tests for create defaults and validation in `backend/src/test/java/com/ticketmanagement/ticket/service/TicketServiceCreateTest.java`: omitted priority becomes `MEDIUM`; status is `OPEN`; `createdAt` equals `updatedAt`; blank or spaces-only title is `Title is required`; title over 200 is `Title must be 200 characters or fewer`; blank description is `Description is required`; description over 5,000 is `Description must be 5,000 characters or fewer`; assignee over 100 is `Assignee must be 100 characters or fewer`; a failed create stores nothing
- [X] T017 [P] [US1] Write failing web-slice tests for `POST /api/v1/tickets` returning 201 with `Location` or 400 problem+json in `backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerCreateTest.java`
- [X] T018 [P] [US1] Write a failing PostgreSQL test that a created ticket survives in `backend/src/integrationTest/java/com/ticketmanagement/ticket/CreateTicketIntegrationTest.java`

### Implementation for User Story 1

- [X] T019 [P] [US1] Create `backend/src/main/java/com/ticketmanagement/ticket/api/dto/CreateTicketRequest.java` with required `title` (1–200 after trim), required `description` (1–5,000 after trim), optional `priority`, and optional `assignee` (at most 100)
- [X] T020 [P] [US1] Create `backend/src/main/java/com/ticketmanagement/ticket/api/dto/TicketResponse.java` with `id`, `title`, `description`, `priority`, `status`, nullable `assignee`, `createdAt`, `updatedAt`, and `comments`
- [X] T021 [US1] Implement `@Transactional` `create` in `backend/src/main/java/com/ticketmanagement/ticket/service/TicketService.java`: trim first, treat a spaces-only value as blank, default omitted priority to `MEDIUM`, force status `OPEN`, and leave assignee empty as Unassigned
- [X] T022 [US1] Implement `POST /api/v1/tickets` returning 201 and `Location` in `backend/src/main/java/com/ticketmanagement/ticket/api/TicketController.java`

**Checkpoint**: User Story 1 API is testable with `./gradlew test` and `CreateTicketIntegrationTest`

---

## Phase 4: User Story 2 - Review the queue (Priority: P1)

**Goal**: The queue lists tickets most recently updated first, 20 per page, with number, title, status, priority, assignee or Unassigned, and last-updated time.

**Independent Test**: Create several tickets and confirm the list order, columns, empty copy, and a second page of 20.

### Tests for User Story 2 ⚠️

- [X] T023 [P] [US2] Write failing repository tests for `updatedAt` descending then `id` descending and page size 20 in `backend/src/test/java/com/ticketmanagement/ticket/persistence/TicketRepositoryListTest.java`
- [X] T024 [P] [US2] Write failing web-slice tests for `GET /api/v1/tickets` in `backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerListTest.java` asserting `size` is 20 and summaries omit `description` and `comments`

### Implementation for User Story 2

- [X] T025 [P] [US2] Create `backend/src/main/java/com/ticketmanagement/ticket/api/dto/TicketSummaryResponse.java` and `backend/src/main/java/com/ticketmanagement/ticket/api/dto/TicketPageResponse.java` with `content`, `page`, `size`, `totalElements`, and `totalPages`
- [X] T026 [US2] Implement the fixed page of 20 ordered by `updatedAt` descending then `id` descending in `backend/src/main/java/com/ticketmanagement/ticket/persistence/TicketRepository.java` and `backend/src/main/java/com/ticketmanagement/ticket/service/TicketService.java`
- [X] T027 [US2] Implement `GET /api/v1/tickets` in `backend/src/main/java/com/ticketmanagement/ticket/api/TicketController.java` using a zero-based `page` query and ignoring any client page size

**Checkpoint**: User Stories 1 and 2 APIs work independently

---

## Phase 5: User Story 3 - View ticket details (Priority: P1)

**Goal**: Opening a ticket shows every field and comments from oldest to newest. A missing id shows `Ticket not found`.

**Independent Test**: Open one stored ticket and a missing id.

### Tests for User Story 3 ⚠️

- [X] T028 [P] [US3] Write failing web-slice tests for `GET /api/v1/tickets/{id}` in `backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerGetTest.java`: 200 with comments ordered by `createdAt` ascending then `id` ascending; 404 `detail` is `Ticket not found`
- [X] T029 [P] [US3] Write a failing PostgreSQL test in `backend/src/integrationTest/java/com/ticketmanagement/ticket/GetTicketIntegrationTest.java`

### Implementation for User Story 3

- [X] T030 [US3] Implement `get` in `backend/src/main/java/com/ticketmanagement/ticket/service/TicketService.java` and `GET /api/v1/tickets/{id}` in `backend/src/main/java/com/ticketmanagement/ticket/api/TicketController.java`

**Checkpoint**: Detail and missing-ticket responses match the contract

---

## Phase 6: User Story 4 - Update ticket details (Priority: P1)

**Goal**: Title, description, priority, and assignee can change without changing status. Invalid input keeps the previous values. A closed ticket refuses edits.

**Independent Test**: Change each field on an Open ticket, clear assignee, send an invalid title, and edit a Closed ticket.

### Tests for User Story 4 ⚠️

- [X] T031 [P] [US4] Write failing service tests in `backend/src/test/java/com/ticketmanagement/ticket/service/TicketServiceUpdateTest.java`: a field save does not change status; `assignee` null or blank becomes Unassigned; a failed update leaves previous values; a Closed ticket returns `Closed tickets cannot be changed. Reopen the ticket first.`
- [X] T032 [P] [US4] Write failing web-slice tests for `PATCH /api/v1/tickets/{id}` in `backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerUpdateTest.java` covering 200, 400, and 409

### Implementation for User Story 4

- [X] T033 [P] [US4] Create `backend/src/main/java/com/ticketmanagement/ticket/api/dto/UpdateTicketRequest.java` where each of `title`, `description`, `priority`, and `assignee` is optional, an omitted field stays unchanged, and `assignee` null or blank clears the assignee
- [X] T034 [US4] Implement `@Transactional` `update` in `backend/src/main/java/com/ticketmanagement/ticket/service/TicketService.java` using the same trim and length messages as create, without changing status, and moving `updatedAt` forward on success
- [X] T035 [US4] Implement `PATCH /api/v1/tickets/{id}` in `backend/src/main/java/com/ticketmanagement/ticket/api/TicketController.java` and reject a request body that includes `status` with 400

**Checkpoint**: Field updates preserve status and reject closed tickets

---

## Phase 7: User Story 5 - Move a ticket through its lifecycle (Priority: P1)

**Goal**: Status changes only through the six allowed pairs. Every other request is rejected and the stored status stays the same.

**Independent Test**: Attempt every allowed transition and at least one forbidden transition.

### Tests for User Story 5 ⚠️

- [X] T036 [P] [US5] Write failing service tests for every allowed and forbidden pair in `backend/src/test/java/com/ticketmanagement/ticket/service/TicketServiceTransitionTest.java`, asserting `updatedAt` moves only on success and an illegal change does not alter title, description, priority, assignee, or comments
- [X] T037 [P] [US5] Write failing web-slice tests for `POST /api/v1/tickets/{id}/transitions` in `backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerTransitionTest.java`, including 409 `detail` `Cannot change status from Open to Closed.`

### Implementation for User Story 5

- [X] T038 [P] [US5] Create `backend/src/main/java/com/ticketmanagement/ticket/api/dto/TransitionRequest.java` with required `status`
- [X] T039 [US5] Implement `@Transactional` `transition` in `backend/src/main/java/com/ticketmanagement/ticket/service/TicketService.java` by calling `TicketLifecycle`, and build `detail` with the display labels Open, In Progress, Resolved, and Closed
- [X] T040 [US5] Implement `POST /api/v1/tickets/{id}/transitions` in `backend/src/main/java/com/ticketmanagement/ticket/api/TicketController.java`

**Checkpoint**: The lifecycle matrix is enforced by unit tests and the HTTP API

---

## Phase 8: User Story 6 - Add a comment (Priority: P2)

**Goal**: A named comment can be added unless the ticket is Closed. The ticket `updatedAt` moves forward.

**Independent Test**: Add a comment, reload the ticket, and confirm order. A blank comment and a Closed ticket store nothing.

### Tests for User Story 6 ⚠️

- [X] T041 [P] [US6] Write failing service tests in `backend/src/test/java/com/ticketmanagement/ticket/service/TicketServiceCommentTest.java`: `authorName` 1–100, `body` 1–2,000, messages `Author name is required`, `Author name must be 100 characters or fewer`, `Comment is required`, and `Comment must be 2,000 characters or fewer`; Closed tickets use `Closed tickets cannot be changed. Reopen the ticket first.`; success moves `updatedAt`
- [X] T042 [P] [US6] Write failing web-slice tests for `POST /api/v1/tickets/{id}/comments` in `backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerCommentTest.java` covering 201, 400, 404, and 409

### Implementation for User Story 6

- [X] T043 [P] [US6] Create `backend/src/main/java/com/ticketmanagement/ticket/api/dto/CreateCommentRequest.java` and `backend/src/main/java/com/ticketmanagement/ticket/api/dto/CommentResponse.java` with required `authorName` (1–100 after trim) and required `body` (1–2,000 after trim)
- [X] T044 [US6] Implement `@Transactional` `addComment` in `backend/src/main/java/com/ticketmanagement/ticket/service/TicketService.java`
- [X] T045 [US6] Implement `POST /api/v1/tickets/{id}/comments` returning 201 in `backend/src/main/java/com/ticketmanagement/ticket/api/TicketController.java`

**Checkpoint**: Comments persist on open tickets and are refused when Closed

---

## Phase 9: User Story 7 - Find tickets (Priority: P2)

**Goal**: Keyword and status narrow the queue together. Keyword search is a case-insensitive substring of title or description and does not search comments.

**Independent Test**: Create tickets with different titles, descriptions, comments, and statuses, then apply keyword, status, both, and clear.

### Tests for User Story 7 ⚠️

- [X] T046 [P] [US7] Write failing repository tests in `backend/src/test/java/com/ticketmanagement/ticket/persistence/TicketRepositorySearchTest.java` using `lower(column) like lower(concat('%', :q, '%'))` on title or description only, AND with status, and treating a blank `q` as no keyword
- [X] T047 [P] [US7] Write failing web-slice tests for `q` and `status` on `GET /api/v1/tickets` in `backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerSearchTest.java`

### Implementation for User Story 7

- [X] T048 [US7] Apply `q` and `status` in `backend/src/main/java/com/ticketmanagement/ticket/persistence/TicketRepository.java` and `backend/src/main/java/com/ticketmanagement/ticket/service/TicketService.java` while keeping page size 20 and the existing sort
- [X] T049 [US7] Accept optional `q` and `status` on `GET /api/v1/tickets` in `backend/src/main/java/com/ticketmanagement/ticket/api/TicketController.java`

**Checkpoint**: Search and filter match FR-016, FR-017, and FR-018

---

## Phase 10: User Story 8 - Keep work after a restart (Priority: P1)

**Goal**: Successfully saved tickets and comments remain after a new application start. A failed save is still absent.

**Independent Test**: Create, update, and comment, start a new application context on the same database, and compare ids and text.

### Tests for User Story 8 ⚠️

- [X] T050 [US8] Write `backend/src/integrationTest/java/com/ticketmanagement/ticket/RestartPersistenceIntegrationTest.java` to save a ticket, update it, add a comment, open a second Spring context on the same PostgreSQL database, and assert the same ids, fields, status, and comment text, plus the absence of a rejected create

**Checkpoint**: Restart persistence is proven on PostgreSQL

---

## Phase 11: User Story 1 - Create a ticket UI (Priority: P1)

**Goal**: The create screen submits through the typed client and shows field messages from the API.

**Independent Test**: Submit a valid ticket and a blank title. Confirm `Title is required` and that priority defaults to Medium.

### Tests for User Story 1 UI ⚠️

- [X] T051 [P] [US1] Write a failing test for a valid submit and for `Title is required` in `frontend/src/app/tickets/new/new-ticket.test.tsx`

### Implementation for User Story 1 UI

- [X] T052 [US1] Implement `createTicket` and problem+json parsing of `detail` and `errors[].message` in `frontend/src/services/ticket-api.ts`
- [X] T053 [US1] Build the create screen in `frontend/src/app/tickets/new/page.tsx` and `frontend/src/components/TicketForm.tsx`, calling only `frontend/src/services/ticket-api.ts`, with priority default Medium

**Checkpoint**: Create works in the browser against the API

---

## Phase 12: User Story 2 - Review the queue UI (Priority: P1)

**Goal**: The home page shows the queue, 20 rows per page, and the empty state.

**Independent Test**: Open the queue with tickets and with none. Confirm columns, Unassigned, order, and paging.

### Tests for User Story 2 UI ⚠️

- [X] T054 [P] [US2] Write a failing test for row columns and the empty state in `frontend/src/components/TicketTable.test.tsx`

### Implementation for User Story 2 UI

- [X] T055 [P] [US2] Map `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `LOW`, `MEDIUM`, `HIGH`, and `URGENT` to Open, In Progress, Resolved, Closed, Low, Medium, High, and Urgent in `frontend/src/services/ticket-labels.ts`
- [X] T056 [US2] Implement `listTickets` in `frontend/src/services/ticket-api.ts`
- [X] T057 [US2] Build `frontend/src/app/page.tsx` and `frontend/src/components/TicketTable.tsx` showing number, title, status, priority, assignee or Unassigned, and last-updated time, with copy `There are no tickets.` and a link to create

**Checkpoint**: The queue page matches User Story 2

---

## Phase 13: User Story 3 - View ticket details UI (Priority: P1)

**Goal**: The detail page shows the full ticket and comments, or `Ticket not found`.

**Independent Test**: Open a real ticket and a missing id.

### Tests for User Story 3 UI ⚠️

- [X] T058 [P] [US3] Write a failing test for visible fields, comment order, and `Ticket not found` in `frontend/src/components/TicketDetail.test.tsx`

### Implementation for User Story 3 UI

- [X] T059 [US3] Implement `getTicket` in `frontend/src/services/ticket-api.ts`
- [X] T060 [US3] Build `frontend/src/app/tickets/[id]/page.tsx` and `frontend/src/components/TicketDetail.tsx`, showing timestamps in the viewer's local time to the minute and no editor when the ticket is missing

**Checkpoint**: Detail and the missing-ticket state match User Story 3

---

## Phase 14: User Story 4 - Update ticket details UI (Priority: P1)

**Goal**: The member can edit fields without sending a status change, and sees the closed-ticket sentence.

**Independent Test**: Save a new title, clear assignee, and attempt an edit on a Closed ticket.

### Tests for User Story 4 UI ⚠️

- [X] T061 [P] [US4] Write a failing test for a field save, clearing assignee, and `Closed tickets cannot be changed. Reopen the ticket first.` in `frontend/src/components/TicketEditForm.test.tsx`

### Implementation for User Story 4 UI

- [X] T062 [US4] Implement `updateTicket` in `frontend/src/services/ticket-api.ts` so the body never includes `status`
- [X] T063 [US4] Build `frontend/src/components/TicketEditForm.tsx` and use it from `frontend/src/app/tickets/[id]/page.tsx`

**Checkpoint**: Field edits from the UI match User Story 4

---

## Phase 15: User Story 5 - Lifecycle UI (Priority: P1)

**Goal**: The screen offers only the next allowed statuses. The API still rejects any other status.

**Independent Test**: For each current status, the visible actions match the six allowed pairs.

### Tests for User Story 5 UI ⚠️

- [X] T064 [P] [US5] Write a failing test that each status renders only its allowed next labels in `frontend/src/components/StatusActions.test.tsx`

### Implementation for User Story 5 UI

- [X] T065 [US5] Implement `transitionTicket` in `frontend/src/services/ticket-api.ts` for `POST /api/v1/tickets/{id}/transitions`
- [X] T066 [US5] Build `frontend/src/components/StatusActions.tsx` and show the API `detail` when a transition is rejected

**Checkpoint**: The UI cannot offer an illegal move

---

## Phase 16: User Story 6 - Add a comment UI (Priority: P2)

**Goal**: The member adds an author and a comment, and sees validation or the closed-ticket sentence.

**Independent Test**: Save a comment, submit a blank body, and try a Closed ticket.

### Tests for User Story 6 UI ⚠️

- [X] T067 [P] [US6] Write a failing test for `Comment is required` and the closed-ticket sentence in `frontend/src/components/CommentForm.test.tsx`

### Implementation for User Story 6 UI

- [X] T068 [US6] Implement `addComment` in `frontend/src/services/ticket-api.ts`
- [X] T069 [US6] Build `frontend/src/components/CommentForm.tsx` and place it on `frontend/src/app/tickets/[id]/page.tsx`

**Checkpoint**: Comment entry matches User Story 6

---

## Phase 17: User Story 7 - Find tickets UI (Priority: P2)

**Goal**: Keyword and status filter the queue together and can be cleared.

**Independent Test**: Apply a keyword, a status, both, a miss, and clear.

### Tests for User Story 7 UI ⚠️

- [X] T070 [P] [US7] Write a failing test for keyword, status, no-match copy `No tickets match.`, and clear in `frontend/src/components/QueueFilters.test.tsx`

### Implementation for User Story 7 UI

- [X] T071 [US7] Send `q`, `status`, and `page` from `listTickets` in `frontend/src/services/ticket-api.ts`, omitting `status` when the member chooses all statuses
- [X] T072 [US7] Build `frontend/src/components/QueueFilters.tsx` and use it from `frontend/src/app/page.tsx`, keeping the filter when moving to another page

**Checkpoint**: Finding tickets matches User Story 7

---

## Phase 18: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T073 Add OpenAPI annotations on `backend/src/main/java/com/ticketmanagement/ticket/api/TicketController.java` so the generated spec matches `specs/001-support-ticket-system/contracts/openapi.yaml`
- [X] T074 [P] Write JSON console logging for a `prod` profile only, with the correlation id, in `backend/src/main/resources/logback-spring.xml`
- [X] T075 Walk scenarios 1–11 in `specs/001-support-ticket-system/quickstart.md` and fix only mismatches with `specs/001-support-ticket-system/spec.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories API (Phases 3–10)**: Depend on Foundational. Complete these before any UI phase
- **User Stories UI (Phases 11–17)**: Depend on the matching API phase and on `frontend/src/services/ticket-api.ts`
- **Polish (Phase 18)**: Depends on the UI stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Starts after Foundational. No other story required
- **User Story 2 (P1)**: API can use repository fixtures. UI needs the list endpoint
- **User Story 3 (P1)**: Needs a stored ticket. Zero comments are valid before User Story 6
- **User Story 4 (P1)**: Needs User Story 1 create behavior
- **User Story 5 (P1)**: Needs `TicketLifecycle` and a stored ticket
- **User Story 6 (P2)**: Needs a stored ticket. Closed-ticket refusal uses the Closed status from User Story 5
- **User Story 7 (P2)**: Extends the User Story 2 list query
- **User Story 8 (P1)**: Runs after User Stories 1, 4, and 6 so the restart test can update and comment
- **UI phases**: Do not start until Phase 10 is complete

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- DTOs before services
- Services before controllers
- API before the UI for that story
- Backend validation and state handling stay in Phases 2–10, before UI polish

### Parallel Opportunities

- T003 and T004 can run in parallel after T001
- T007, T009, and T010 can run in parallel once T006 is started
- Test tasks marked [P] inside one story touch different files and can run together
- Do not edit `TicketService.java` or `TicketController.java` in parallel; those tasks stay sequential

---

## Parallel Example: User Story 1

```bash
# Failing tests together, before implementation:
Task: "T016 TicketServiceCreateTest in backend/src/test/java/com/ticketmanagement/ticket/service/TicketServiceCreateTest.java"
Task: "T017 TicketControllerCreateTest in backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerCreateTest.java"
Task: "T018 CreateTicketIntegrationTest in backend/src/integrationTest/java/com/ticketmanagement/ticket/CreateTicketIntegrationTest.java"

# DTOs together, after those tests exist:
Task: "T019 CreateTicketRequest in backend/src/main/java/com/ticketmanagement/ticket/api/dto/CreateTicketRequest.java"
Task: "T020 TicketResponse in backend/src/main/java/com/ticketmanagement/ticket/api/dto/TicketResponse.java"
```

## Parallel Example: User Story 5

```bash
Task: "T036 TicketServiceTransitionTest in backend/src/test/java/com/ticketmanagement/ticket/service/TicketServiceTransitionTest.java"
Task: "T037 TicketControllerTransitionTest in backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerTransitionTest.java"
```

## Parallel Example: User Story 7

```bash
Task: "T046 TicketRepositorySearchTest in backend/src/test/java/com/ticketmanagement/ticket/persistence/TicketRepositorySearchTest.java"
Task: "T047 TicketControllerSearchTest in backend/src/test/java/com/ticketmanagement/ticket/api/TicketControllerSearchTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 API
4. **STOP and VALIDATE**: `./gradlew test` and `CreateTicketIntegrationTest`
5. Add Phase 11 only if a create screen is needed for the demo

### Incremental Delivery

1. Setup + Foundational
2. API stories in order: create, queue, detail, update, lifecycle, comments, search, restart
3. UI stories in the same order
4. Polish and the quickstart walk
5. Each API story stays testable without the UI

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. After Phase 2, split only tasks marked [P] that do not share `TicketService.java` or `TicketController.java`
3. One developer owns those two files so status rules stay in one place
4. UI work starts after Phase 10

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
- Allowed status moves are only the six pairs in T008. Do not add `CANCELLED`

## Phase 19: Convergence

- [X] T076 Refresh ticket detail after a successful comment so the Updated time matches the server per FR-015 and US6 (partial)
- [X] T077 Show author-name validation errors on the comment form per FR-014 and FR-020 (partial)
