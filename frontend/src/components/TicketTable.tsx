import Link from "next/link";
import type { TicketSummary } from "../services/ticket-api";
import { formatWhen, priorityLabel, statusLabel } from "../services/ticket-labels";

export function TicketTable({ tickets }: { tickets: TicketSummary[] }) {
  if (tickets.length === 0) {
    return (
      <div>
        <p>There are no tickets.</p>
        <Link className="button" href="/tickets/new">
          Create a ticket
        </Link>
      </div>
    );
  }
  return (
    <table>
      <thead>
        <tr>
          <th>Number</th>
          <th>Title</th>
          <th>Status</th>
          <th>Priority</th>
          <th>Assignee</th>
          <th>Updated</th>
        </tr>
      </thead>
      <tbody>
        {tickets.map((ticket) => (
          <tr key={ticket.id}>
            <td>{ticket.id}</td>
            <td>
              <Link href={`/tickets/${ticket.id}`}>{ticket.title}</Link>
            </td>
            <td>{statusLabel(ticket.status)}</td>
            <td>{priorityLabel(ticket.priority)}</td>
            <td>{ticket.assignee ?? "Unassigned"}</td>
            <td>{formatWhen(ticket.updatedAt)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
