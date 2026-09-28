# Implementation Plan: Inline Queue Ticket Management UI

**Branch**: `002-inline-queue-ui` | **Date**: 2026-09-29 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-inline-queue-ui/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Update only the queue screen (`/`): rename heading and tab to **Ticket Management**, place **Create Ticket** on the same row as the heading (right-aligned), add inline create (tick/cross) and inline edit (edit icon, tick/cross) in the ticket table, and show validation with an error border only on invalid inline fields. Reuse the existing API client and ticket rules from `001-support-ticket-system`. No backend changes.

## Technical Context

**Language/Version**: TypeScript strict; Node.js 20+; existing Next.js 16.3.6 / React 19 frontend

**Primary Dependencies**: Next.js App Router, existing `frontend/src/services/ticket-api.ts`, `ticket-labels.ts`

**Storage**: N/A (client state only; persistence via existing REST API)

**Testing**: Vitest and React Testing Library optional for new components; `npm run lint`, `type-check`, `build`; manual scenarios in [quickstart.md](./quickstart.md)

**Target Platform**: Modern desktop browser

**Project Type**: Web application — frontend delta on existing monorepo

**Performance Goals**: Same as SC-003/SC-004 in base system; inline rows must not block list fetch

**Constraints**: FR-012 freezes filters, columns, paging, copy, and detail links. All HTTP through `ticket-api.ts`. Exact validation messages from base spec. No new dependencies unless required for icons.

**Scale/Scope**: One screen (`QueueScreen` + `TicketTable` and new inline row components). Three user stories, all P1.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Plan | Result |
|------|------|--------|
| I. UI uses typed API client only | Inline create/edit call `createTicket` / `updateTicket` in `ticket-api.ts` | Pass |
| II. REST/OpenAPI | No API changes | Pass (N/A) |
| III. Vitest / RTL available | Optional component tests; not required by 002 spec | Pass |
| IV. PostgreSQL / Flyway | No database work | Pass (N/A) |
| V. ESLint, Prettier, `npm run build` | Required in quickstart | Pass |

No violations. Complexity tracking empty.

### Post-design re-check

[contracts/queue-screen-ui.md](./contracts/queue-screen-ui.md) documents queue-only behavior. [data-model.md](./data-model.md) adds client drafts only. No new API resources. Still pass.

## Project Structure

### Documentation (this feature)

```text
specs/002-inline-queue-ui/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── queue-screen-ui.md
└── tasks.md
```

### Source Code (touch points)

```text
frontend/
├── src/app/
│   ├── layout.tsx              # unchanged unless needed
│   └── page.tsx                # metadata title for queue (if not only in QueueScreen)
├── src/components/
│   ├── QueueScreen.tsx         # header row, inline session state, refresh list
│   ├── TicketTable.tsx         # edit icons, inline rows, empty + table modes
│   ├── InlineCreateRow.tsx     # new (or equivalent)
│   ├── InlineEditCells.tsx     # new (or equivalent)
│   └── QueueFilters.tsx        # unchanged behavior
├── src/app/globals.css         # .field-invalid border style
└── src/services/ticket-api.ts  # unchanged signatures
```

**Structure Decision**: All implementation under `frontend/`. Backend and `compose.yaml` untouched.

## Delivery sequence

`/speckit-tasks` expands this into `tasks.md`.

1. Shared styles and types for inline drafts and `field-invalid` borders.
2. User Story 1: heading, tab title, header row layout.
3. User Story 2: inline create row, session limits, create + validation UX.
4. User Story 3: inline edit per row, closed-ticket handling, validation UX.
5. Polish: quickstart walk, confirm FR-012 regressions, `npm run build`.

## Out of scope

Backend changes, new API fields, status inline edit, description inline edit, delete, new routes, removing `/tickets/new` or detail pages, design system overhaul beyond error borders and icon buttons.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
