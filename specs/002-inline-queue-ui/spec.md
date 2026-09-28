# Feature Specification: Inline Queue Ticket Management UI

**Feature Branch**: `002-inline-queue-ui`

**Created**: 2026-09-29

**Status**: Draft

**Input**: User description: "Apply only these UI changes to the ticket management screen: rename heading and browser tab to Ticket Management; move Create Ticket to the right of the heading on the same row; enable inline ticket creation with confirm (tick) and cancel (cross); enable inline editing per row with an edit icon; on validation failure highlight only invalid inline fields with an error border; keep the rest of the UI unchanged."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Queue page identity and layout (Priority: P1)

A team member opens the ticket queue and sees a clear page title, a matching browser tab label, and a primary create action aligned with the heading.

**Why this priority**: Establishes the screen the other inline behaviors build on.

**Independent Test**: Open the queue and confirm the visible heading, browser tab title, and placement of Create Ticket without using inline create or edit.

**Acceptance Scenarios**:

1. **Given** the queue is open, **When** the member reads the page heading, **Then** it reads "Ticket Management".
2. **Given** the queue is open, **When** the member looks at the browser tab title, **Then** it reads "Ticket Management".
3. **Given** the queue is open, **When** the member views the top of the page, **Then** "Ticket Management" and a control labeled "Create Ticket" appear on the same horizontal row, with the heading on the left and Create Ticket on the right.

---

### User Story 2 - Inline ticket creation (Priority: P1)

A team member creates a ticket directly in the queue table without leaving the page.

**Why this priority**: Removes navigation friction for the most common action on the queue.

**Independent Test**: From the queue, start inline create, enter valid values, confirm with the tick control, and see a new Open ticket in the list with the same rules as the existing create flow.

**Acceptance Scenarios**:

1. **Given** the queue is open and no inline create row is active, **When** the member chooses Create Ticket, **Then** an empty editable row appears in the ticket table with the same default field values as the existing create flow (including priority Medium when not yet chosen).
2. **Given** an inline create row is visible, **When** the member fills title, description, priority, and optional assignee and chooses the tick (confirm) control, **Then** a new Open ticket is stored and the row becomes a normal list row with a new ticket number.
3. **Given** an inline create row is visible, **When** the member chooses the cross (cancel) control, **Then** the row is removed and no ticket is stored.
4. **Given** an inline create row with invalid or incomplete data, **When** the member chooses the tick control, **Then** no ticket is stored and each invalid field is shown with an error border only on that field, using the same validation messages as the existing ticket system.
5. **Given** an inline create row is already open, **When** the member chooses Create Ticket again, **Then** the system does not open a second inline create row until the current one is confirmed or cancelled.

---

### User Story 3 - Inline ticket editing in the queue (Priority: P1)

A team member updates common ticket fields from the queue without opening the detail view.

**Why this priority**: Speeds correction of title, priority, and assignee while triaging the list.

**Independent Test**: On an Open ticket row, start inline edit, change title and assignee, confirm, and see the row reflect stored values; status and ticket number stay unchanged.

**Acceptance Scenarios**:

1. **Given** a ticket row in the queue, **When** the member is not already editing another row, **Then** an edit icon is available on that row.
2. **Given** a ticket in Open, In Progress, or Resolved, **When** the member chooses the edit icon, **Then** the row shows editable controls for title, priority, and assignee while number, status, and last-updated time remain visible as on a normal row (status is not editable inline).
3. **Given** inline edit is active on a row, **When** the member changes values and chooses the tick control, **Then** only title, priority, and assignee are saved, status is unchanged, and last-updated time moves forward per existing ticket rules.
4. **Given** inline edit is active, **When** the member chooses the cross control, **Then** the row returns to its previous displayed values and nothing is stored from that attempt.
5. **Given** a Closed ticket row, **When** the member tries to start inline edit, **Then** the change is refused with "Closed tickets cannot be changed. Reopen the ticket first." and the row stays read-only.
6. **Given** inline edit with invalid data, **When** the member chooses the tick control, **Then** previously stored values remain unchanged and only fields that failed validation show an error border.

---

### Edge Cases

- Inline create and inline edit on different rows at the same time: only one inline create row and one inline edit row may be active at once; starting another inline action cancels or blocks until the current inline session ends (confirm, cancel, or successful save).
- Confirm with all defaults on inline create: title and description are still required; blank required fields get an error border and the specific messages from the existing validation rules.
- Pagination, keyword search, and status filter: unchanged; an inline create row appears on the current page and does not clear active filters.
- Opening a ticket title link to the detail view: unchanged; inline edit does not remove navigation to full ticket details.
- Failed inline save: only invalid fields are visually highlighted with an error border; valid fields in the same row keep normal styling.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The queue page heading MUST display "Ticket Management".
- **FR-002**: The browser tab title for the queue page MUST be "Ticket Management".
- **FR-003**: The queue page MUST show "Ticket Management" and a "Create Ticket" control on one row, heading left and Create Ticket right.
- **FR-004**: Choosing Create Ticket MUST insert one inline editable row in the ticket table with default values matching the existing create flow (including default priority Medium).
- **FR-005**: The inline create row MUST collect title, description, priority, and optional assignee before confirm, and MUST apply the same create validation rules and messages as the existing ticket system.
- **FR-006**: The inline create row MUST offer a tick control to confirm and a cross control to cancel; confirm stores a new Open ticket on success, cancel discards the row with no stored ticket.
- **FR-007**: Each ticket row in the queue MUST show an edit icon that starts inline edit for that row when allowed.
- **FR-008**: Inline edit MUST allow changing title, priority, and assignee only; MUST NOT change status, ticket number, or created time inline; MUST follow existing field-update validation and closed-ticket rules.
- **FR-009**: Inline edit MUST offer a tick control to save and a cross control to cancel; cancel MUST restore the row display to values before edit started.
- **FR-010**: On any failed inline create or inline edit validation, the system MUST show an error border only on each invalid field in that inline row; fields that passed validation MUST NOT receive an error border.
- **FR-011**: Validation messages for inline create and inline edit MUST remain the specific messages defined for the existing ticket system (for example "Title is required", "Closed tickets cannot be changed. Reopen the ticket first.").
- **FR-012**: Aside from the requirements in FR-001 through FR-011, the queue screen MUST keep existing behavior unchanged: keyword and status filters, Apply and Clear, column set (number, title, status, priority, assignee, updated), sort order, paging, empty-queue copy, no-match copy, and links into ticket detail.

### Key Entities

- **Inline create session**: A temporary row in the queue table where the member enters a new ticket before confirm or cancel.
- **Inline edit session**: A temporary edit mode on one existing queue row for title, priority, and assignee.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A team member can create a valid ticket from the queue using only inline create, without opening a separate create page, in under 1 minute.
- **SC-002**: A team member can update title, priority, and assignee on an Open ticket from the queue using inline edit in under 30 seconds.
- **SC-003**: In a review of inline validation failures, 100% of cases show an error border only on fields that failed validation, not on valid fields in the same row.
- **SC-004**: After inline changes ship, keyword search, status filter, paging, and ticket detail behavior match the existing ticket system in manual checks (no regressions on unchanged areas).

## Assumptions

- This feature extends the existing Support Ticket Management System; all ticket data rules, lifecycle, persistence, search, and detail-page behavior outside FR-012 remain as already specified there.
- Inline create includes description because creation requires it under existing rules, even though description is not a queue table column; inputs may use additional space in or below the row without changing which columns are listed when not editing.
- Inline edit in the queue covers title, priority, and assignee only; description changes remain on the ticket detail view.
- Icon-only tick, cross, and edit controls are sufficient if their purpose is clear from placement and context; no new statuses or filters are introduced.
- Separate create or detail routes may remain available; this feature does not require removing them unless the queue already depended on them only for create—queue empty-state create affordances may still exist alongside header Create Ticket.
- Only one inline create row and one inline edit row may be active at a time on the queue page.
