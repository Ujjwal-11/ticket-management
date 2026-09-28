"use client";

import { FormEvent, useState } from "react";
import { addComment, errorDetail, fieldErrors, type Comment } from "../services/ticket-api";
import type { TicketStatus } from "../services/ticket-labels";

export function CommentForm({
  ticketId,
  status,
  onCreated,
}: {
  ticketId: number;
  status: TicketStatus;
  onCreated: (comment: Comment) => void | Promise<void>;
}) {
  const [authorName, setAuthorName] = useState("");
  const [body, setBody] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [detail, setDetail] = useState("");

  if (status === "CLOSED") {
    return <p className="error">Closed tickets cannot be changed. Reopen the ticket first.</p>;
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setErrors({});
    setDetail("");
    try {
      const comment = await addComment(ticketId, authorName, body);
      setAuthorName("");
      setBody("");
      await onCreated(comment);
    } catch (error) {
      setErrors(fieldErrors(error));
      setDetail(errorDetail(error));
    }
  }

  return (
    <form onSubmit={onSubmit}>
      <h2>Add comment</h2>
      {detail ? <p className="error">{detail}</p> : null}
      <label>
        Author name
        <input value={authorName} onChange={(event) => setAuthorName(event.target.value)} />
      </label>
      <label>
        Comment
        <textarea value={body} onChange={(event) => setBody(event.target.value)} />
      </label>
      {errors.authorName ? <p className="error">{errors.authorName}</p> : null}
      {errors.body ? <p className="error">{errors.body}</p> : null}
      <button type="submit">Add comment</button>
    </form>
  );
}
