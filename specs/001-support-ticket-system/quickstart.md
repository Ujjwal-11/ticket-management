# Quickstart: Support Ticket Management System

Validate the running system against [spec.md](./spec.md). Field rules are in [data-model.md](./data-model.md). HTTP shapes are in [contracts/openapi.yaml](./contracts/openapi.yaml).

## Prerequisites

- JDK 21
- Node.js 20 LTS
- Docker, for PostgreSQL 16

## Setup

From the repository root:

```bash
docker compose -f compose.yaml up -d
```

Backend:

```bash
cd backend
./gradlew bootRun
```

The API listens on `http://localhost:8080`. OpenAPI UI is `http://localhost:8080/swagger-ui.html`.

Frontend, in a second shell:

```bash
cd frontend
npm install
npm run dev
```

The UI listens on `http://localhost:3000` and calls the API only through `src/services/ticket-api.ts`.

## Automated checks

Backend:

```bash
cd backend
./gradlew test
./gradlew integrationTest
./gradlew check
```

`test` runs lifecycle, service, web-slice, and data-slice tests. `integrationTest` runs the full HTTP journey on PostgreSQL. `check` includes both and Spotless.

Frontend:

```bash
cd frontend
npm run lint
npm run type-check
npm test
npm run build
```

## Manual scenarios

Use the UI unless a step says to call the API. Expected sentences are exact.

1. **Create.** Submit a title, description, and priority. The queue shows a new Open ticket with that title, priority, and Unassigned when assignee was left empty. Omit priority and confirm Medium.
2. **Reject invalid create.** Submit a blank title. The ticket is not created. The message is `Title is required`.
3. **Queue.** With several tickets, the most recently updated is first. Each row shows number, title, status, priority, assignee or Unassigned, and last-updated time. With no tickets, the page says there are no tickets and offers create. With more than 20, the next page still respects the current search and filter.
4. **Detail.** Open a ticket. Title, description, priority, status, assignee, both times, and comments from oldest to newest are visible.
5. **Missing ticket.** Open `/tickets/999999`. The page shows `Ticket not found` and no editor.
6. **Update.** On an Open ticket, change title, description, priority, and assignee. Status stays Open. Clear assignee and confirm Unassigned. Reopen the ticket and confirm the new values.
7. **Closed edit.** Move a ticket to Closed through the legal path, then try to edit a field. The message is `Closed tickets cannot be changed. Reopen the ticket first.` Stored values stay the same.
8. **Lifecycle.** From the UI, only the next legal statuses are offered. Walk Open → In Progress → Resolved → Closed, In Progress → Open, Resolved → In Progress, and Closed → In Progress. Then call the API directly:

```bash
curl -s -X POST http://localhost:8080/api/v1/tickets/1/transitions \
  -H 'Content-Type: application/json' \
  -d '{"status":"CLOSED"}'
```

Use an Open ticket id. The status stays Open. `detail` is `Cannot change status from Open to Closed.`
9. **Comment.** On a ticket that is not Closed, add an author and a comment. Leave and return. The comment remains, and the ticket's last-updated time has moved. A blank comment shows `Comment is required` and stores nothing. A Closed ticket refuses the comment with the closed-ticket sentence.
10. **Search and filter.** A keyword matches title or description in any letter case and does not match comment text. A status limits the rows. Both together require both. No match says that no tickets match. Clearing both shows the full queue.
11. **Restart.** Create a ticket, update it, and add a comment. Stop `bootRun` and start it again against the same database. Number, fields, status, and comments match what was saved.

## Done when

- `./gradlew check` passes.
- The four frontend commands pass.
- Scenarios 1–11 match the sentences in the spec.
