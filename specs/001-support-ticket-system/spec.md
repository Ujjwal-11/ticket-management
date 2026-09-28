# Feature Specification: Support Ticket Management System

**Feature Branch**: `001-support-ticket-system`

**Created**: 2026-09-29

**Status**: Draft

**Input**: User description: "Support Ticket Management System covering create, list, view, and update of tickets (title, description, priority, assignee), comments, keyword search, status filter, rejection of invalid input with clear messages, data that survives a restart, and a strict ticket lifecycle."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a ticket (Priority: P1)

A team member records a new support request with a title, a description, and a priority. The request appears in the shared queue as a new open ticket.

**Why this priority**: Nothing else in the system has value until a ticket exists.

**Independent Test**: Submit a valid request and confirm a new open ticket appears in the queue with the entered title, description, and priority.

**Acceptance Scenarios**:

1. **Given** the queue is open, **When** the member submits a title, description, and priority, **Then** a new ticket is stored with a unique number, status Open, the chosen priority, and the time it was created.
2. **Given** the create form, **When** the member leaves assignee empty, **Then** the ticket is stored as Unassigned.
3. **Given** the create form, **When** the member enters an assignee name, **Then** that name is stored on the new ticket and the status is still Open.
4. **Given** the create form, **When** the member omits priority, **Then** the ticket is stored with priority Medium.
5. **Given** the create form, **When** the title or description is blank, or any field breaks the limits in FR-003, **Then** the ticket is not created and each problem is named in a specific message.

---

### User Story 2 - Review the queue (Priority: P1)

A team member opens the queue and sees every ticket, newest activity first, with enough information to choose what to open.

**Why this priority**: The queue is how the team finds work after tickets exist.

**Independent Test**: Create several tickets and confirm the list shows number, title, status, priority, assignee, and last-updated time, with the most recently updated ticket first.

**Acceptance Scenarios**:

1. **Given** at least one ticket, **When** the member opens the queue, **Then** each row shows ticket number, title, status, priority, assignee or Unassigned, and last-updated time.
2. **Given** tickets updated at different times, **When** the member opens the queue, **Then** the most recently updated ticket is first.
3. **Given** no tickets, **When** the member opens the queue, **Then** the page states that the queue is empty and offers a way to create a ticket.
4. **Given** more than 20 tickets, **When** the member opens the queue, **Then** the first page shows 20 tickets and the member can move to later pages.

---

### User Story 3 - View ticket details (Priority: P1)

A team member opens one ticket and reads the full request and its comments.

**Why this priority**: Updates and comments depend on seeing the current ticket.

**Independent Test**: Open one ticket from the queue and confirm every stored field and comment is visible.

**Acceptance Scenarios**:

1. **Given** a ticket with comments, **When** the member opens it, **Then** they see number, title, description, priority, status, assignee or Unassigned, created time, last-updated time, and comments from oldest to newest.
2. **Given** a ticket number that does not exist, **When** the member tries to open it, **Then** they see "Ticket not found" and no editable form.

---

### User Story 4 - Update ticket details (Priority: P1)

A team member corrects the title, description, priority, or assignee without changing the ticket status.

**Why this priority**: Intake data is often incomplete and must be fixable while work is underway.

**Independent Test**: Change each editable field on an open ticket and confirm the new values remain after leaving and reopening the ticket, while status stays the same.

**Acceptance Scenarios**:

1. **Given** a ticket in Open, In Progress, or Resolved, **When** the member changes title, description, priority, or assignee and saves, **Then** only those fields change, status is unchanged, and last-updated time moves forward.
2. **Given** a ticket with an assignee, **When** the member clears the assignee and saves, **Then** the ticket shows Unassigned.
3. **Given** an invalid title, description, priority, or assignee, **When** the member saves, **Then** the previous values stay stored and each problem is named in a specific message.
4. **Given** a Closed ticket, **When** the member tries to change title, description, priority, or assignee, **Then** the change is refused with "Closed tickets cannot be changed. Reopen the ticket first."

---

### User Story 5 - Move a ticket through its lifecycle (Priority: P1)

A team member advances, sends back, closes, or reopens a ticket only along the allowed path.

**Why this priority**: Unrestricted status changes make the queue unreliable.

**Independent Test**: Attempt every allowed transition and at least one forbidden transition, and confirm only the allowed ones change status.

**Acceptance Scenarios**:

1. **Given** a ticket in Open, **When** the member marks it In Progress, **Then** the status becomes In Progress.
2. **Given** a ticket in In Progress, **When** the member marks it Resolved, **Then** the status becomes Resolved.
3. **Given** a ticket in In Progress, **When** the member sends it back to Open, **Then** the status becomes Open.
4. **Given** a ticket in Resolved, **When** the member closes it, **Then** the status becomes Closed.
5. **Given** a ticket in Resolved, **When** the member returns it to In Progress, **Then** the status becomes In Progress.
6. **Given** a ticket in Closed, **When** the member reopens it, **Then** the status becomes In Progress and field edits are allowed again.
7. **Given** any ticket, **When** the member requests a transition that is not in the lifecycle table, **Then** the status stays the same and the message is "Cannot change status from {current} to {requested}."

---

### User Story 6 - Add a comment (Priority: P2)

A team member adds a named note to a ticket that is not closed.

**Why this priority**: Discussion matters, but the queue is useful before comments exist.

**Independent Test**: Add a comment to an open ticket, leave the page, and confirm the comment is still there in order.

**Acceptance Scenarios**:

1. **Given** a ticket in Open, In Progress, or Resolved, **When** the member submits an author name and a comment, **Then** the comment is stored on that ticket with the time it was written, and the ticket's last-updated time moves forward.
2. **Given** the comment form, **When** the author name or comment is blank, or either field breaks the limits in FR-014, **Then** nothing is stored and each problem is named in a specific message.
3. **Given** a Closed ticket, **When** the member tries to add a comment, **Then** the comment is refused with "Closed tickets cannot be changed. Reopen the ticket first."

---

### User Story 7 - Find tickets (Priority: P2)

A team member narrows the queue by a keyword, a status, or both.

**Why this priority**: Search matters once the queue is large. Create, view, and update already work without it.

**Independent Test**: Create tickets with different titles, descriptions, and statuses, then confirm keyword and status narrowing return only the expected rows.

**Acceptance Scenarios**:

1. **Given** tickets whose title or description contains a word, **When** the member searches that word in any letter case, **Then** only matching tickets appear.
2. **Given** a status filter, **When** the member chooses one status, **Then** only tickets in that status appear.
3. **Given** a keyword and a status, **When** the member applies both, **Then** a ticket appears only if it matches the keyword and the status.
4. **Given** a keyword that matches nothing, **When** the member searches, **Then** the page states that no tickets match.
5. **Given** a search or filter, **When** the member clears it, **Then** the full queue is shown again.

---

### User Story 8 - Keep work after a restart (Priority: P1)

A team member stops the system and starts it again, and every ticket and comment is still there.

**Why this priority**: A queue that forgets its work cannot be used for real support.

**Independent Test**: Create a ticket, update it, add a comment, restart the system, and confirm number, fields, status, and comments match what was saved.

**Acceptance Scenarios**:

1. **Given** saved tickets and comments, **When** the system is stopped and started again, **Then** the same tickets, numbers, fields, statuses, and comments are available.
2. **Given** a failed save because input was invalid, **When** the system is restarted, **Then** that failed save is still absent.

---

### Edge Cases

- Title, description, author name, assignee, and comment are trimmed before checks. A value that is only spaces is treated as blank.
- Duplicate titles are allowed. Ticket number, not title, identifies a ticket.
- Two people saving the same ticket: the later save wins. There is no merge and no warning in this version.
- Search matches title and description only. Comment text is not searched.
- A keyword and a status filter apply together. Priority is visible in the list but is not a filter.
- An illegal status change does not change title, description, priority, assignee, or comments.
- A failed field update does not change status. A failed comment does not change ticket fields.
- Page navigation stays inside the current search and filter.
- The member cannot type a status, ticket number, created time, or last-updated time when creating a ticket.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST let a team member create a ticket with a title, a description, an optional assignee name, and a priority. If priority is omitted, it MUST be Medium.
- **FR-002**: A new ticket MUST receive a unique ticket number, status Open, a created time, and a last-updated time equal to the created time. The member MUST NOT supply the number, status, or either time.
- **FR-003**: The system MUST reject a create or field update, store nothing from that attempt, and show a specific message when:
  - title is blank or longer than 200 characters ("Title is required" or "Title must be 200 characters or fewer")
  - description is blank or longer than 5,000 characters ("Description is required" or "Description must be 5,000 characters or fewer")
  - priority is not Low, Medium, High, or Urgent ("Priority must be Low, Medium, High, or Urgent")
  - assignee is longer than 100 characters ("Assignee must be 100 characters or fewer")
- **FR-004**: The queue MUST list tickets most recently updated first and show ticket number, title, status, priority, assignee or Unassigned, and last-updated time.
- **FR-005**: An empty queue MUST say that there are no tickets and offer a way to create one.
- **FR-006**: The queue MUST show 20 tickets per page and MUST let the member open later pages. Page navigation MUST keep the active keyword and status filter.
- **FR-007**: Opening a ticket MUST show number, title, description, priority, status, assignee or Unassigned, created time, last-updated time, and comments from oldest to newest.
- **FR-008**: A request for a missing ticket MUST show "Ticket not found" and MUST NOT present an editor.
- **FR-009**: While a ticket is Open, In Progress, or Resolved, the member MUST be able to change title, description, priority, and assignee without changing status.
- **FR-010**: Clearing assignee MUST store the ticket as Unassigned.
- **FR-011**: A field update that fails validation MUST leave the previously stored values unchanged.
- **FR-012**: Status MUST change only through the lifecycle below. Any other change MUST be refused, leave status unchanged, and show "Cannot change status from {current} to {requested}."

  | From | Allowed next status |
  |------|---------------------|
  | Open | In Progress |
  | In Progress | Resolved, Open |
  | Resolved | Closed, In Progress |
  | Closed | In Progress |

- **FR-013**: While a ticket is Closed, the system MUST refuse changes to title, description, priority, assignee, and MUST refuse new comments, with the message "Closed tickets cannot be changed. Reopen the ticket first."
- **FR-014**: The member MUST be able to add a comment with an author name and a body while the ticket is Open, In Progress, or Resolved. Author name MUST be 1 to 100 characters. Body MUST be 1 to 2,000 characters. A blank or oversized value MUST be rejected with "Author name is required", "Author name must be 100 characters or fewer", "Comment is required", or "Comment must be 2,000 characters or fewer", and nothing from that attempt is stored.
- **FR-015**: Adding a comment, saving a field change, or changing status MUST move the ticket's last-updated time forward. Created time and ticket number MUST NOT change.
- **FR-016**: Keyword search MUST be case-insensitive and MUST match a partial word in the title or the description. It MUST NOT match comment text.
- **FR-017**: The member MUST be able to limit the queue to one status: Open, In Progress, Resolved, or Closed. The member MUST be able to show all statuses.
- **FR-018**: When a keyword and a status are both set, a ticket MUST appear only if it matches both. When nothing matches, the page MUST say that no tickets match. Clearing search and filter MUST show the full queue.
- **FR-019**: After the system stops and starts again, every successfully saved ticket, field, status, and comment MUST still be available with the same ticket numbers.
- **FR-020**: Validation failures, illegal status changes, edits to a closed ticket, and missing tickets MUST each show the specific message named in this specification. A generic failure message MUST NOT be used for those cases.

### Key Entities

- **Ticket**: One support request. Attributes: unique ticket number, title, description, priority (Low, Medium, High, Urgent), status (Open, In Progress, Resolved, Closed), assignee name or Unassigned, created time, last-updated time. A ticket has zero or more comments.
- **Comment**: One note on one ticket. Attributes: author name, body, time written. A comment belongs to exactly one ticket and cannot be edited or deleted in this version.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A team member can create a valid ticket in under 1 minute.
- **SC-002**: A team member can find a known ticket among 1,000 tickets by keyword or status and open it in under 30 seconds.
- **SC-003**: The first page of a 1,000-ticket queue is visible within 2 seconds of opening the queue.
- **SC-004**: 100% of status changes outside the lifecycle table are refused, and the stored status stays the same.
- **SC-005**: 100% of tickets and comments that were successfully saved are still present, with the same numbers and wording, after a restart.
- **SC-006**: At least 95% of first-time team members complete create, update, and comment on the first attempt without outside help.
- **SC-007**: Every rejected create, update, comment, status change, and missing-ticket lookup shows the specific message named in the requirements.

## Assumptions

- Users are members of one internal support team. Everyone who can open the system can create, view, update, comment on, and change the status of any ticket.
- Sign-in, roles, and a directory of people are out of scope. Assignee and comment author are names typed by the member, not accounts.
- A new ticket starts at Open. Priority defaults to Medium when the member does not choose one.
- Allowed statuses are only Open, In Progress, Resolved, and Closed. Allowed moves are only those in FR-012.
- Closed means frozen. The only way to change a closed ticket is to reopen it to In Progress.
- Comments cannot be edited or deleted. Tickets cannot be deleted.
- Search covers title and description only. The only filter is status. Priority, assignee, and date are not filters in this version.
- The queue is ordered by last-updated time, newest first, and shows 20 tickets per page.
- Simultaneous edits use last save wins, with no conflict warning.
- Notifications, file attachments, due dates, and service targets are out of scope.
- How the system is built is decided outside this specification. This document defines what team members can do and how the queue behaves.
