"use client";

import { updateTicket, fieldErrors } from "../services/ticket-api";
import { formatWhen, priorityLabel, statusLabel, type Priority } from "../services/ticket-labels";
import type { TicketSummary } from "../services/ticket-api";
import {
  fieldInvalidClass,
  fieldMessage,
  type InlineEditDraft,
} from "./inline-queue-state";

const PRIORITIES: Priority[] = ["LOW", "MEDIUM", "HIGH", "URGENT"];

type Props = {
  ticket: TicketSummary;
  draft: InlineEditDraft;
  onDraftChange: (draft: InlineEditDraft) => void;
  onSuccess: () => void;
  onCancel: () => void;
};

export function InlineEditRow({ ticket, draft, onDraftChange, onSuccess, onCancel }: Props) {
  function update(partial: Partial<InlineEditDraft>) {
    onDraftChange({ ...draft, ...partial, fieldErrors: {} });
  }

  async function confirm() {
    try {
      await updateTicket(ticket.id, {
        title: draft.title,
        priority: draft.priority,
        assignee: draft.assignee.trim() === "" ? null : draft.assignee,
      });
      onSuccess();
    } catch (error) {
      onDraftChange({ ...draft, fieldErrors: fieldErrors(error) });
    }
  }

  return (
    <tr className="inline-edit-row">
      <td>{ticket.id}</td>
      <td>
        <input
          aria-label="Title"
          className={fieldInvalidClass("title", draft.fieldErrors)}
          value={draft.title}
          onChange={(event) => update({ title: event.target.value })}
        />
        {fieldMessage("title", draft.fieldErrors) ? (
          <p className="error">{fieldMessage("title", draft.fieldErrors)}</p>
        ) : null}
      </td>
      <td>{statusLabel(ticket.status)}</td>
      <td>
        <select
          aria-label="Priority"
          className={fieldInvalidClass("priority", draft.fieldErrors)}
          value={draft.priority}
          onChange={(event) => update({ priority: event.target.value as Priority })}
        >
          {PRIORITIES.map((value) => (
            <option key={value} value={value}>
              {priorityLabel(value)}
            </option>
          ))}
        </select>
        {fieldMessage("priority", draft.fieldErrors) ? (
          <p className="error">{fieldMessage("priority", draft.fieldErrors)}</p>
        ) : null}
      </td>
      <td>
        <input
          aria-label="Assignee"
          className={fieldInvalidClass("assignee", draft.fieldErrors)}
          value={draft.assignee}
          onChange={(event) => update({ assignee: event.target.value })}
        />
        {fieldMessage("assignee", draft.fieldErrors) ? (
          <p className="error">{fieldMessage("assignee", draft.fieldErrors)}</p>
        ) : null}
      </td>
      <td>{formatWhen(ticket.updatedAt)}</td>
      <td className="inline-actions">
        <button type="button" className="icon-button" aria-label="Confirm" onClick={() => confirm()}>
          ✓
        </button>
        <button type="button" className="icon-button" aria-label="Cancel" onClick={onCancel}>
          ✕
        </button>
      </td>
    </tr>
  );
}
