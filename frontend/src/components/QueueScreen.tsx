"use client";

import { useCallback, useEffect, useState } from "react";
import { listTickets, type TicketSummary } from "../services/ticket-api";
import type { TicketStatus } from "../services/ticket-labels";
import {
  emptyCreateDraft,
  type InlineCreateDraft,
  type InlineEditDraft,
} from "./inline-queue-state";
import { QueueFilters } from "./QueueFilters";
import { TicketTable } from "./TicketTable";

export function QueueScreen() {
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState<TicketStatus | "">("");
  const [page, setPage] = useState(0);
  const [tickets, setTickets] = useState<TicketSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [createActive, setCreateActive] = useState(false);
  const [createDraft, setCreateDraft] = useState<InlineCreateDraft>(emptyCreateDraft());
  const [editDraft, setEditDraft] = useState<InlineEditDraft | null>(null);
  const [closedNotice, setClosedNotice] = useState("");
  const filtering = keyword.trim() !== "" || status !== "";
  const editingTicketId = editDraft?.ticketId ?? null;

  const reloadTickets = useCallback(() => {
    return listTickets({ q: keyword, status, page }).then((result) => {
      setTickets(result.content);
      setTotalPages(result.totalPages);
    });
  }, [keyword, status, page]);

  useEffect(() => {
    let active = true;
    reloadTickets().then(() => {
      if (!active) {
        return;
      }
    });
    return () => {
      active = false;
    };
  }, [reloadTickets]);

  function startCreate() {
    if (createActive) {
      return;
    }
    setClosedNotice("");
    setCreateActive(true);
    setCreateDraft(emptyCreateDraft());
  }

  function cancelCreate() {
    setCreateActive(false);
    setCreateDraft(emptyCreateDraft());
  }

  function finishCreate() {
    setCreateActive(false);
    setCreateDraft(emptyCreateDraft());
    void reloadTickets();
  }

  function requestEdit(ticket: TicketSummary) {
    if (ticket.status === "CLOSED") {
      setClosedNotice("Closed tickets cannot be changed. Reopen the ticket first.");
      return;
    }
    if (editDraft !== null) {
      return;
    }
    setClosedNotice("");
    setEditDraft({
      ticketId: ticket.id,
      title: ticket.title,
      priority: ticket.priority,
      assignee: ticket.assignee ?? "",
      originalTitle: ticket.title,
      originalPriority: ticket.priority,
      originalAssignee: ticket.assignee,
      fieldErrors: {},
    });
  }

  function cancelEdit() {
    setEditDraft(null);
  }

  function finishEdit() {
    setEditDraft(null);
    void reloadTickets();
  }

  const hideTableForNoMatch = filtering && tickets.length === 0 && !createActive;

  return (
    <section>
      <div className="queue-header">
        <h1>Ticket Management</h1>
        <button type="button" onClick={startCreate}>
          Create Ticket
        </button>
      </div>
      <QueueFilters
        keyword={keyword}
        status={status}
        resultCount={tickets.length}
        filtering={filtering}
        onApply={(nextKeyword, nextStatus) => {
          setKeyword(nextKeyword);
          setStatus(nextStatus);
          setPage(0);
        }}
        onClear={() => {
          setKeyword("");
          setStatus("");
          setPage(0);
        }}
      />
      {closedNotice ? <p className="error">{closedNotice}</p> : null}
      {hideTableForNoMatch ? null : (
        <TicketTable
          tickets={tickets}
          createActive={createActive}
          createDraft={createDraft}
          onCreateDraftChange={setCreateDraft}
          onCreateSuccess={finishCreate}
          onCreateCancel={cancelCreate}
          editingTicketId={editingTicketId}
          editDraft={editDraft}
          onEditDraftChange={setEditDraft}
          onEditSuccess={finishEdit}
          onEditCancel={cancelEdit}
          onRequestEdit={requestEdit}
        />
      )}
      <div className="row">
        <button type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>
          Previous
        </button>
        <button
          type="button"
          disabled={page + 1 >= totalPages}
          onClick={() => setPage((current) => current + 1)}
        >
          Next
        </button>
      </div>
    </section>
  );
}
