# Quickstart: Inline Queue Ticket Management UI

Validates [spec.md](./spec.md) on top of the running app from `specs/001-support-ticket-system/quickstart.md`. No new backend setup.

## Prerequisites

- Backend `./gradlew bootRun` on port 8080
- PostgreSQL via `docker compose -f compose.yaml up -d`
- Frontend `cd frontend && npm run dev` on port 3000

## Automated checks (frontend only)

```bash
cd frontend
npm run lint
npm run type-check
npm run build
```

Optional after implementation adds tests:

```bash
npm test
```

Backend regression (unchanged API):

```bash
cd backend
GRADLE_USER_HOME=$HOME/.gradle ./gradlew check
```

## Manual scenarios

1. **Chrome.** Open `http://localhost:3000/`. Heading reads `Ticket Management`. Browser tab reads `Ticket Management`. `Create Ticket` is on the same row as the heading, aligned right.
2. **Inline create.** Click `Create Ticket`. Fill title, description, priority, assignee; click tick. New Open ticket appears in the list. Click `Create Ticket`, click cross; row disappears with no new ticket.
3. **Inline create validation.** Start inline create; leave title blank; click tick. Title field has error border only; message `Title is required`. No ticket created.
4. **Inline edit.** On an Open ticket, click edit; change title and assignee; click tick. Row updates; status unchanged. Open detail page and confirm same values.
5. **Inline edit cancel.** Start edit, change title, click cross. Row shows previous title.
6. **Closed ticket.** On a Closed ticket, inline edit is not available or shows closed message; no save.
7. **Unchanged queue.** Keyword + status filter, Apply, Clear, paging, and title link to detail still work. Empty queue still shows `There are no tickets.` when no inline row is shown.

## Done when

- Scenarios 1–7 pass.
- `npm run lint`, `npm run type-check`, and `npm run build` pass.
- `./gradlew check` still passes (no backend edits).
