# UI Contract: Queue screen (inline ticket management)

**Feature**: `002-inline-queue-ui`  
**Route**: `/` (queue)  
**API client**: `frontend/src/services/ticket-api.ts` only

## Chrome

| ID | Requirement |
|----|-------------|
| QC-001 | Visible `h1` text is exactly `Ticket Management`. |
| QC-002 | Document title for `/` is exactly `Ticket Management`. |
| QC-003 | `Create Ticket` and the heading share one row; heading left, button right. |

## Table (unchanged columns when not editing)

| Column | Idle behavior |
|--------|----------------|
| Number | Ticket id, read-only |
| Title | Link to `/tickets/{id}` |
| Status | Label from `ticket-labels` |
| Priority | Label |
| Assignee | Name or `Unassigned` |
| Updated | Local formatted time |
| Actions | Edit icon when edit allowed |

Filters (`Keyword`, `Status`, Apply, Clear), pagination (Previous/Next), empty copy (`There are no tickets.`), and no-match copy (`No tickets match.`) behave as in feature `001`.

## Inline create

| ID | Trigger | Behavior |
|----|---------|----------|
| IC-001 | Click `Create Ticket` | Insert inline create row at top of table body; defaults priority Medium. |
| IC-002 | Tick | Call `createTicket`; on success refresh list and remove inline row. |
| IC-003 | Cross | Remove inline row; no API call. |
| IC-004 | Tick with validation errors | No create; error border only on invalid fields; show spec messages. |
| IC-005 | `Create Ticket` while create row open | No second create row. |

Inputs: title, description, priority, assignee (optional).

## Inline edit

| ID | Trigger | Behavior |
|----|---------|----------|
| IE-001 | Edit icon on allowed row | Row becomes editable for title, priority, assignee. |
| IE-002 | Tick | Call `updateTicket`; on success refresh row/list; status unchanged. |
| IE-003 | Cross | Restore row to values before edit. |
| IE-004 | Closed row | No inline edit; show `Closed tickets cannot be changed. Reopen the ticket first.` if user attempts edit. |
| IE-005 | Tick with validation errors | Stored values unchanged; error border only on invalid fields. |
| IE-006 | Second edit while one active | Block until current edit session ends. |

## Visual validation

| ID | Rule |
|----|------|
| V-001 | Class `field-invalid` (or equivalent) sets **border only** on invalid controls. |
| V-002 | Valid controls in the same inline row do not get `field-invalid`. |
| V-003 | Message text matches existing ticket system strings from API `errors[].message` or `detail`. |
