"use client";

import { FormEvent, useState } from "react";
import { errorDetail, fieldErrors, updateTicket, type Ticket } from "../services/ticket-api";
import { priorityLabel, type Priority } from "../services/ticket-labels";

export function TicketEditForm({ ticket, onUpdated }: { ticket: Ticket; onUpdated: (ticket: Ticket) => void }) {
  const [title, setTitle] = useState(ticket.title);
  const [description, setDescription] = useState(ticket.description);
  const [priority, setPriority] = useState<Priority>(ticket.priority);
  const [assignee, setAssignee] = useState(ticket.assignee ?? "");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [detail, setDetail] = useState("");
  const closed = ticket.status === "CLOSED";

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setErrors({});
    setDetail("");
    try {
      const updated = await updateTicket(ticket.id, {
        title,
        description,
        priority,
        assignee: assignee.trim() === "" ? null : assignee,
      });
      onUpdated(updated);
    } catch (error) {
      setErrors(fieldErrors(error));
      setDetail(errorDetail(error));
    }
  }

  if (closed) {
    return <p className="error">Closed tickets cannot be changed. Reopen the ticket first.</p>;
  }

  return (
    <form onSubmit={onSubmit}>
      <h2>Edit ticket</h2>
      {detail ? <p className="error">{detail}</p> : null}
      <label>
        Title
        <input value={title} onChange={(event) => setTitle(event.target.value)} />
      </label>
      {errors.title ? <p className="error">{errors.title}</p> : null}
      <label>
        Description
        <textarea value={description} onChange={(event) => setDescription(event.target.value)} />
      </label>
      <label>
        Priority
        <select aria-label="Priority" value={priority} onChange={(event) => setPriority(event.target.value as Priority)}>
          <option value="LOW">{priorityLabel("LOW")}</option>
          <option value="MEDIUM">{priorityLabel("MEDIUM")}</option>
          <option value="HIGH">{priorityLabel("HIGH")}</option>
          <option value="URGENT">{priorityLabel("URGENT")}</option>
        </select>
      </label>
      <label>
        Assignee
        <input value={assignee} onChange={(event) => setAssignee(event.target.value)} />
      </label>
      <button type="submit">Save</button>
    </form>
  );
}
