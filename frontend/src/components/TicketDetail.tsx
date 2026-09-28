"use client";

import { useState } from "react";
import { getTicket, type Ticket } from "../services/ticket-api";
import { formatWhen, priorityLabel, statusLabel } from "../services/ticket-labels";
import { CommentForm } from "./CommentForm";
import { StatusActions } from "./StatusActions";
import { TicketEditForm } from "./TicketEditForm";

export function TicketDetail({ ticket: initial, notFound }: { ticket?: Ticket; notFound?: boolean }) {
  const [ticket, setTicket] = useState(initial);

  if (notFound || !ticket) {
    return <p>Ticket not found</p>;
  }

  return (
    <article>
      <h1>{ticket.title}</h1>
      <p>Number {ticket.id}</p>
      <p>{ticket.description}</p>
      <p>Status {statusLabel(ticket.status)}</p>
      <p>Priority {priorityLabel(ticket.priority)}</p>
      <p>Assignee {ticket.assignee ?? "Unassigned"}</p>
      <p>Created {formatWhen(ticket.createdAt)}</p>
      <p>Updated {formatWhen(ticket.updatedAt)}</p>
      <StatusActions ticket={ticket} onUpdated={setTicket} />
      <TicketEditForm ticket={ticket} onUpdated={setTicket} />
      <h2>Comments</h2>
      <ol>
        {ticket.comments.map((comment) => (
          <li key={comment.id}>
            {comment.authorName}: {comment.body}
          </li>
        ))}
      </ol>
      <CommentForm
        ticketId={ticket.id}
        status={ticket.status}
        onCreated={async () => {
          setTicket(await getTicket(String(ticket.id)));
        }}
      />
    </article>
  );
}
