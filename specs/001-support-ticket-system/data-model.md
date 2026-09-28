# Data Model: Support Ticket Management System

Source: [spec.md](./spec.md). Physical rules from [research.md](./research.md).

## Ticket

One support request.

| Field | Type | Rules |
|-------|------|--------|
| id | integer | Generated. This is the ticket number. Never supplied by the client. Stable across restart. |
| title | string | Required after trim. 1–200 characters. |
| description | string | Required after trim. 1–5,000 characters. |
| priority | enum | `LOW`, `MEDIUM`, `HIGH`, `URGENT`. Omitted on create becomes `MEDIUM`. |
| status | enum | `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`. Create always stores `OPEN`. |
| assignee | string or empty | Optional. Trimmed. 1–100 characters when present. Empty means Unassigned. |
| createdAt | timestamp | Set once at create. Never changes. |
| updatedAt | timestamp | Set to `createdAt` on create. Moves forward on field save, status change, and new comment. |

A ticket has zero or more comments. Tickets are not deleted.

### Display labels

| Stored | Shown to the member |
|--------|---------------------|
| OPEN | Open |
| IN_PROGRESS | In Progress |
| RESOLVED | Resolved |
| CLOSED | Closed |
| LOW | Low |
| MEDIUM | Medium |
| HIGH | High |
| URGENT | Urgent |

Status error text uses the shown labels: `Cannot change status from Open to Closed.`

## Comment

One note on one ticket. Comments are not edited or deleted.

| Field | Type | Rules |
|-------|------|--------|
| id | integer | Generated. |
| ticket id | integer | Required. The parent ticket. |
| authorName | string | Required after trim. 1–100 characters. |
| body | string | Required after trim. 1–2,000 characters. |
| createdAt | timestamp | Set once when the comment is saved. |

Order on the ticket: `createdAt` ascending, then `id` ascending.

## Lifecycle

```text
Open → In Progress → Resolved → Closed
In Progress → Open
Resolved → In Progress
Closed → In Progress
```

| From | Allowed |
|------|---------|
| OPEN | IN_PROGRESS |
| IN_PROGRESS | RESOLVED, OPEN |
| RESOLVED | CLOSED, IN_PROGRESS |
| CLOSED | IN_PROGRESS |

Any other requested status, including the current status, is rejected. Title, description, priority, assignee, and comments stay as they were.

While status is `CLOSED`, field updates and new comments are rejected with `Closed tickets cannot be changed. Reopen the ticket first.` Reopen is the transition to `IN_PROGRESS`. After that, edits and comments are allowed again.

## Validation messages

| Condition | Message |
|-----------|---------|
| Blank title | Title is required |
| Title over 200 | Title must be 200 characters or fewer |
| Blank description | Description is required |
| Description over 5,000 | Description must be 5,000 characters or fewer |
| Priority not in the enum | Priority must be Low, Medium, High, or Urgent |
| Assignee over 100 | Assignee must be 100 characters or fewer |
| Blank author | Author name is required |
| Author over 100 | Author name must be 100 characters or fewer |
| Blank comment | Comment is required |
| Comment over 2,000 | Comment must be 2,000 characters or fewer |
| Unknown ticket | Ticket not found |
| Illegal status | Cannot change status from {current} to {requested}. |
| Edit or comment when closed | Closed tickets cannot be changed. Reopen the ticket first. |

Trim before checks. A value that is only spaces is blank. A failed create, update, or comment stores nothing from that attempt.

## Query rules

- Queue order: `updatedAt` descending, then `id` descending.
- Page size: 20. The client cannot change it.
- Keyword: case-insensitive substring of `title` or `description`. Comment text is not searched. Blank keyword applies no text filter.
- Status filter: one status, or all when omitted.
- Keyword and status both apply when both are set.
- Duplicate titles are allowed.

## Tables

`tickets`

- `id` bigserial primary key
- `title` varchar(200) not null
- `description` varchar(5000) not null
- `priority` varchar(16) not null
- `status` varchar(16) not null
- `assignee` varchar(100) null
- `created_at` timestamptz not null
- `updated_at` timestamptz not null
- index `(updated_at desc, id desc)`

`comments`

- `id` bigserial primary key
- `ticket_id` bigint not null references `tickets(id)`
- `author_name` varchar(100) not null
- `body` varchar(2000) not null
- `created_at` timestamptz not null
- index `(ticket_id, created_at, id)`

Migration: `backend/src/main/resources/db/migration/V1__create_tickets.sql`. No later manual schema edits.

## Domain placement

`TicketLifecycle` holds the allowed pairs and throws when a pair is absent. It has no Spring or database types. `TicketService` is the only writer of status, fields, and comments, and each public method is `@Transactional`. Controllers do not contain these rules.
