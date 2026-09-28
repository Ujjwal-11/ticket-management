# Data Model: Inline Queue UI (client state)

No new server entities. Ticket and Comment remain as in `specs/001-support-ticket-system/data-model.md`. This document describes **queue-page client state** only.

## InlineCreateDraft

Temporary values while inline create is open.

| Field | Type | Default | Notes |
|-------|------|---------|--------|
| title | string | `""` | Trimmed on submit; blank → `Title is required` |
| description | string | `""` | Not shown as a column when idle; required on create |
| priority | enum | `MEDIUM` | Low, Medium, High, Urgent |
| assignee | string | `""` | Optional; blank → Unassigned |
| fieldErrors | map field → message | `{}` | Drives error border + message text |
| active | boolean | `false` | Only one active create session |

On successful `createTicket`, draft clears and list refreshes via `listTickets`.

## InlineEditDraft

Snapshot and edits for one queue row.

| Field | Type | Notes |
|-------|------|--------|
| ticketId | number | Row being edited |
| originalTitle | string | For cancel restore |
| originalPriority | Priority | For cancel restore |
| originalAssignee | string \| null | For cancel restore |
| title | string | Editable |
| priority | Priority | Editable |
| assignee | string | Editable; blank clears to Unassigned |
| fieldErrors | map field → message | Border + messages on failed save |
| active | boolean | At most one edit session |

On tick: `updateTicket(id, { title, priority, assignee })`. On cross: discard draft and re-render row from last loaded summary.

## Queue chrome

| Element | Value |
|---------|--------|
| pageHeading | `Ticket Management` |
| documentTitle | `Ticket Management` (queue route) |
| createButtonLabel | `Create Ticket` |

## Validation mapping (inline)

Server field names from RFC 7807 `errors[].field` map to inline controls:

| field | Inline create | Inline edit |
|-------|---------------|-------------|
| title | yes | yes |
| description | yes | no |
| priority | yes | yes |
| assignee | yes | yes |

Closed ticket attempts use `detail` message `Closed tickets cannot be changed. Reopen the ticket first.` (no field borders unless API returns field errors).
