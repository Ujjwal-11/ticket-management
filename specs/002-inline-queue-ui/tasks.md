---

description: "Task list for inline queue UI feature"
---

# Tasks: Inline Queue Ticket Management UI

**Input**: Design documents from `/specs/002-inline-queue-ui/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Not required by feature spec; validate with `npm run lint`, `type-check`, `build`, and [quickstart.md](./quickstart.md).

**Organization**: Three P1 user stories. Frontend only. No backend tasks.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Web UI: `frontend/src/`
- Contract reference: `specs/002-inline-queue-ui/contracts/queue-screen-ui.md`

## Phase 1: Setup (Shared UI infrastructure)

**Purpose**: Styles and shared types for inline validation borders

- [X] T001 Add `.field-invalid` (or equivalent) in `frontend/src/app/globals.css` so only the control border changes on validation failure, without altering valid fields in the same row
- [X] T002 [P] Add inline draft types and helpers (`InlineCreateDraft`, `InlineEditDraft`, map `errors[].field` to control names) in `frontend/src/components/inline-queue-state.ts` per `specs/002-inline-queue-ui/data-model.md`

---

## Phase 2: Foundational (Blocking prerequisites)

**Purpose**: Session rules shared by inline create and edit

- [X] T003 Lift inline create and inline edit session state into `frontend/src/components/QueueScreen.tsx`: at most one create row, at most one edit row, allow both types simultaneously, block a second edit until the current edit ends, and refresh `listTickets` after successful create or update

**Checkpoint**: Queue screen can own inline sessions before row UI is built

---

## Phase 3: User Story 1 - Queue page identity and layout (Priority: P1) 🎯 MVP

**Goal**: Heading, browser tab, and Create Ticket placement per FR-001–FR-003.

**Independent Test**: Open `/` and verify heading `Ticket Management`, tab title `Ticket Management`, and Create Ticket on the right of the same row as the heading (QC-001–QC-003).

### Implementation for User Story 1

- [X] T004 [US1] Set queue document title to `Ticket Management` via `metadata` in `frontend/src/app/page.tsx` or the queue entry component per research.md
- [X] T005 [US1] Replace the queue `h1` text with `Ticket Management` and label the primary button `Create Ticket` on one `.row` with heading left and button right in `frontend/src/components/QueueScreen.tsx`

**Checkpoint**: Chrome matches spec without inline rows yet

---

## Phase 4: User Story 2 - Inline ticket creation (Priority: P1)

**Goal**: Create tickets from an inline table row with tick and cross controls.

**Independent Test**: Start inline create, submit valid data with tick, see new Open ticket; cancel with cross; invalid title shows border only on title (IC-001–IC-005).

### Implementation for User Story 2

- [X] T006 [P] [US2] Implement `frontend/src/components/InlineCreateRow.tsx` with inputs for title, description, priority (default Medium), and assignee, plus tick and cross icon buttons with accessible labels
- [X] T007 [US2] Wire Create Ticket in `frontend/src/components/QueueScreen.tsx` to open a single inline create row; ignore a second click while create is active; show table header and create row when the queue is empty
- [X] T008 [US2] On tick in `InlineCreateRow.tsx`, call `createTicket` from `frontend/src/services/ticket-api.ts`, apply `field-invalid` only to fields returned in `errors[]`, show exact API messages, clear row and refresh list on success
- [X] T009 [US2] On cross, remove the inline create row without calling the API from `frontend/src/components/QueueScreen.tsx`

**Checkpoint**: Inline create works end-to-end on the queue page

---

## Phase 5: User Story 3 - Inline ticket editing in the queue (Priority: P1)

**Goal**: Edit title, priority, and assignee per row with edit, tick, and cross.

**Independent Test**: Edit an Open ticket row, save with tick, cancel with cross; Closed row refuses edit (IE-001–IE-006).

### Implementation for User Story 3

- [X] T010 [P] [US3] Implement `frontend/src/components/InlineEditRow.tsx` (or inline cells) for editable title, priority, and assignee with tick and cross controls while number, status, and Updated stay read-only
- [X] T011 [US3] Add an edit icon on each eligible row in `frontend/src/components/TicketTable.tsx`; hide or disable for Closed tickets and show `Closed tickets cannot be changed. Reopen the ticket first.` when edit is attempted
- [X] T012 [US3] On tick, call `updateTicket` from `frontend/src/services/ticket-api.ts`, apply `field-invalid` only to failing fields, leave stored values unchanged on failure, and refresh the row on success
- [X] T013 [US3] On cross, restore the row from the edit session snapshot without calling the API

**Checkpoint**: Inline edit works; title links to detail remain

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: FR-012 regression and release checks

- [X] T014 Confirm `frontend/src/components/QueueFilters.tsx` behavior unchanged (keyword, status, Apply, Clear, `No tickets match.`)
- [X] T015 Confirm empty-queue copy `There are no tickets.` and pagination controls in `frontend/src/components/QueueScreen.tsx` still work with active filters
- [X] T016 Walk scenarios 1–7 in `specs/002-inline-queue-ui/quickstart.md` and fix only spec mismatches in `frontend/src/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Start immediately
- **Foundational (Phase 2)**: Depends on T001–T002
- **US1 (Phase 3)**: Depends on Phase 2
- **US2 (Phase 4)**: Depends on US1 header (T005) and Phase 2 session state
- **US3 (Phase 5)**: Depends on Phase 2; can follow US2 (shared `TicketTable`)
- **Polish (Phase 6)**: After US2 and US3

### User Story Dependencies

- **US1**: No dependency on US2/US3
- **US2**: Uses US1 button placement; independent test via inline create
- **US3**: Independent test via inline edit; shares `TicketTable` with US2 — implement US2 before US3 or coordinate `TicketTable.tsx` edits sequentially

### Parallel Opportunities

- T001 and T002 in parallel
- T006 and T010 in parallel once Phase 2 is done (different new files)
- Do not edit `QueueScreen.tsx` and `TicketTable.tsx` in parallel across stories

---

## Parallel Example: User Story 2

```bash
Task: "T006 InlineCreateRow.tsx"
# Then sequentially: T007–T009 in QueueScreen / TicketTable integration
```

## Parallel Example: User Story 3

```bash
Task: "T010 InlineEditRow.tsx"
# Then sequentially: T011–T013 in TicketTable.tsx"
```

---

## Implementation Strategy

### MVP First (User Story 1 only)

1. Complete Phase 1–2
2. Complete Phase 3 (T004–T005)
3. Stop and verify chrome on `http://localhost:3000/`

### Incremental Delivery

1. Setup + Foundational + US1 → visible rebrand and layout
2. US2 → inline create
3. US3 → inline edit
4. Polish → quickstart + FR-012 checks

### Scope guard

- No backend files
- No changes to ticket lifecycle, search, or detail screens except shared styles
- No new npm dependencies unless icon approach requires it (prefer Unicode/SVG)

---

## Notes

- Validation messages come from the API; do not invent new strings
- `field-invalid` is border-only per FR-010; message text still required per FR-011
- Feature `001-support-ticket-system` remains the source of ticket business rules
