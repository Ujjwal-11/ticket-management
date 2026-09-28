# Research: Inline Queue Ticket Management UI

## Scope

**Decision**: Frontend-only change on the existing queue screen. No backend, API, or database changes.

**Rationale**: FR-001–FR-012 are presentation and interaction on the queue. Create and update still use `createTicket` and `updateTicket` from `frontend/src/services/ticket-api.ts` with the same validation messages as feature `001-support-ticket-system`.

**Alternatives considered**: New REST endpoints for partial queue updates — rejected; existing PATCH and POST already match inline edit and create fields.

## Page title and heading

**Decision**: Set the browser tab title to `Ticket Management` via Next.js `metadata` on the queue route (`frontend/src/app/page.tsx`). Keep root `layout.tsx` metadata generic or align only if needed for other routes.

**Rationale**: Spec FR-002 applies to the queue page. Detail and create routes can keep their own titles so FR-012 “unchanged” detail behavior stays isolated.

**Alternatives considered**: Change root layout title globally — would affect `/tickets/new` and `/tickets/[id]` without a spec requirement.

## Header layout

**Decision**: One horizontal header row in `QueueScreen`: `h1` “Ticket Management” left, `Create Ticket` button right, using existing `.row` flex patterns.

**Rationale**: Matches FR-003 and screenshot intent without new layout framework.

## Inline create row

**Decision**: When the queue is empty, choosing Create Ticket still shows the table header plus one inline create row (empty state copy may remain above or beside the table; filters unchanged). Inline create is a dedicated row component inserted at the top of `tbody`, with a second table row or cell span for **description** (required by create rules but not a list column).

**Rationale**: Spec FR-005 requires description; assumptions allow space in or below the row. Two-row pattern keeps column headers FR-012 lists unchanged.

**Defaults**: Priority `MEDIUM`, blank title, description, assignee; status not editable (always Open on save).

**Session rule**: At most one inline create row; a second click on Create Ticket does nothing until confirm or cancel (FR-004, US2 scenario 5).

## Inline edit row

**Decision**: Edit icon on each data row starts inline edit for **title**, **priority**, and **assignee** only. Tick saves via `updateTicket`; cross restores values from session start. Closed tickets: no edit icon, or icon disabled with closed message on attempt per FR-008.

**Rationale**: Matches US3 and assumptions; description stays on detail page.

**Session rule**: At most one inline edit row at a time; starting edit on another row is blocked until the current edit ends (confirm, cancel, or successful save).

**Coexistence**: One inline create and one inline edit may be active together (spec assumptions).

## Validation UX

**Decision**: On API `ApiError` with `errors[]`, map `field` to inputs and apply a CSS class (e.g. `.field-invalid`) that changes **border only** on those fields. Still render the exact `errors[].message` text near the field or row (FR-011). Do not add error border to fields that passed validation (FR-010).

**Rationale**: User asked for border-only emphasis on invalid fields; spec still requires named messages.

## Icons

**Decision**: Use accessible icon buttons: tick (confirm), cross (cancel), pencil (edit). Unicode or inline SVG with `aria-label` (`Confirm`, `Cancel`, `Edit ticket`).

**Rationale**: Spec allows icon-only controls when purpose is clear; no new icon library required (constitution: minimal dependencies).

## API usage

**Decision**: All HTTP remains in `ticket-api.ts`. Inline components call `createTicket`, `updateTicket`, and `listTickets` only through that module.

**Rationale**: Constitution I — UI must not call fetch directly outside the typed client.

## Regression guard

**Decision**: Manual and quickstart checks confirm filters, paging, sort, empty copy, no-match copy, and title links to detail are unchanged (FR-012).

**Alternatives considered**: Playwright — out of scope per project convention.
