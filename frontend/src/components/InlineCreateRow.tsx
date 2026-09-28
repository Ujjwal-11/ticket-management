"use client";

import { createTicket, fieldErrors } from "../services/ticket-api";
import type { Priority } from "../services/ticket-labels";
import {
  fieldInvalidClass,
  fieldMessage,
  type InlineCreateDraft,
} from "./inline-queue-state";

const PRIORITIES: Priority[] = ["LOW", "MEDIUM", "HIGH", "URGENT"];

type Props = {
  draft: InlineCreateDraft;
  onDraftChange: (draft: InlineCreateDraft) => void;
  onSuccess: () => void;
  onCancel: () => void;
};

export function InlineCreateRow({ draft, onDraftChange, onSuccess, onCancel }: Props) {
  function update(partial: Partial<InlineCreateDraft>) {
    onDraftChange({ ...draft, ...partial, fieldErrors: {} });
  }

  async function confirm() {
    try {
      await createTicket({
        title: draft.title,
        description: draft.description,
        priority: draft.priority,
        assignee: draft.assignee.trim() === "" ? null : draft.assignee,
      });
      onSuccess();
    } catch (error) {
      onDraftChange({ ...draft, fieldErrors: fieldErrors(error) });
    }
  }

  return (
    <>
      <tr className="inline-create-row">
        <td>—</td>
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
        <td>Open</td>
        <td>
          <select
            aria-label="Priority"
            className={fieldInvalidClass("priority", draft.fieldErrors)}
            value={draft.priority}
            onChange={(event) => update({ priority: event.target.value as Priority })}
          >
            {PRIORITIES.map((value) => (
              <option key={value} value={value}>
                {value === "LOW"
                  ? "Low"
                  : value === "MEDIUM"
                    ? "Medium"
                    : value === "HIGH"
                      ? "High"
                      : "Urgent"}
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
        <td>—</td>
        <td className="inline-actions">
          <button type="button" className="icon-button" aria-label="Confirm" onClick={() => confirm()}>
            ✓
          </button>
          <button type="button" className="icon-button" aria-label="Cancel" onClick={onCancel}>
            ✕
          </button>
        </td>
      </tr>
      <tr className="inline-create-description">
        <td colSpan={7}>
          <label>
            Description
            <textarea
              aria-label="Description"
              className={fieldInvalidClass("description", draft.fieldErrors)}
              value={draft.description}
              onChange={(event) => update({ description: event.target.value })}
              rows={2}
            />
          </label>
          {fieldMessage("description", draft.fieldErrors) ? (
            <p className="error">{fieldMessage("description", draft.fieldErrors)}</p>
          ) : null}
        </td>
      </tr>
    </>
  );
}
