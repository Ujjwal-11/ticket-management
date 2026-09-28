"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, createTicket, fieldErrors } from "../services/ticket-api";
import type { Priority } from "../services/ticket-labels";

export function TicketForm() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<Priority>("MEDIUM");
  const [assignee, setAssignee] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setErrors({});
    try {
      const created = await createTicket({
        title,
        description,
        priority,
        assignee: assignee.trim() === "" ? null : assignee,
      });
      router.push(`/tickets/${created.id}`);
    } catch (error) {
      setErrors(fieldErrors(error));
      if (!(error instanceof ApiError)) {
        setErrors({ form: "Something went wrong" });
      }
    }
  }

  return (
    <form onSubmit={onSubmit}>
      <h1>Create ticket</h1>
      <label>
        Title
        <input value={title} onChange={(event) => setTitle(event.target.value)} />
      </label>
      {errors.title ? <p className="error">{errors.title}</p> : null}
      <label>
        Description
        <textarea value={description} onChange={(event) => setDescription(event.target.value)} />
      </label>
      {errors.description ? <p className="error">{errors.description}</p> : null}
      <label>
        Priority
        <select value={priority} onChange={(event) => setPriority(event.target.value as Priority)}>
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
          <option value="URGENT">Urgent</option>
        </select>
      </label>
      <label>
        Assignee
        <input value={assignee} onChange={(event) => setAssignee(event.target.value)} />
      </label>
      <button type="submit">Create</button>
    </form>
  );
}
