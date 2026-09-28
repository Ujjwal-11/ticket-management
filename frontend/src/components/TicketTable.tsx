import Link from "next/link";
import type { TicketSummary } from "../services/ticket-api";
import { formatWhen, priorityLabel, statusLabel } from "../services/ticket-labels";
import { InlineCreateRow } from "./InlineCreateRow";
import { InlineEditRow } from "./InlineEditRow";
import type { InlineCreateDraft, InlineEditDraft } from "./inline-queue-state";

type TicketTableProps = {
  tickets: TicketSummary[];
  createActive: boolean;
  createDraft: InlineCreateDraft;
  onCreateDraftChange: (draft: InlineCreateDraft) => void;
  onCreateSuccess: () => void;
  onCreateCancel: () => void;
  editingTicketId: number | null;
  editDraft: InlineEditDraft | null;
  onEditDraftChange: (draft: InlineEditDraft) => void;
  onEditSuccess: () => void;
  onEditCancel: () => void;
  onRequestEdit: (ticket: TicketSummary) => void;
};

export function TicketTable({
  tickets,
  createActive,
  createDraft,
  onCreateDraftChange,
  onCreateSuccess,
  onCreateCancel,
  editingTicketId,
  editDraft,
  onEditDraftChange,
  onEditSuccess,
  onEditCancel,
  onRequestEdit,
}: TicketTableProps) {
  const showTable = createActive || tickets.length > 0;

  if (!showTable) {
    return <p>There are no tickets.</p>;
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
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {createActive ? (
          <InlineCreateRow
            draft={createDraft}
            onDraftChange={onCreateDraftChange}
            onSuccess={onCreateSuccess}
            onCancel={onCreateCancel}
          />
        ) : null}
        {tickets.map((ticket) => {
          if (editingTicketId === ticket.id && editDraft) {
            return (
              <InlineEditRow
                key={ticket.id}
                ticket={ticket}
                draft={editDraft}
                onDraftChange={onEditDraftChange}
                onSuccess={onEditSuccess}
                onCancel={onEditCancel}
              />
            );
          }
          return (
            <tr key={ticket.id}>
              <td>{ticket.id}</td>
              <td>
                <Link href={`/tickets/${ticket.id}`}>{ticket.title}</Link>
              </td>
              <td>{statusLabel(ticket.status)}</td>
              <td>{priorityLabel(ticket.priority)}</td>
              <td>{ticket.assignee ?? "Unassigned"}</td>
              <td>{formatWhen(ticket.updatedAt)}</td>
              <td className="inline-actions">
                <button
                  type="button"
                  className="icon-button"
                  aria-label="Edit ticket"
                  disabled={editingTicketId !== null && editingTicketId !== ticket.id}
                  onClick={() => onRequestEdit(ticket)}
                >
                  ✎
                </button>
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}
