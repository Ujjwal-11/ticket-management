"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { listTickets, type TicketSummary } from "../services/ticket-api";
import type { TicketStatus } from "../services/ticket-labels";
import { QueueFilters } from "./QueueFilters";
import { TicketTable } from "./TicketTable";

export function QueueScreen() {
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState<TicketStatus | "">("");
  const [page, setPage] = useState(0);
  const [tickets, setTickets] = useState<TicketSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const filtering = keyword.trim() !== "" || status !== "";

  useEffect(() => {
    let active = true;
    listTickets({ q: keyword, status, page }).then((result) => {
      if (!active) {
        return;
      }
      setTickets(result.content);
      setTotalPages(result.totalPages);
    });
    return () => {
      active = false;
    };
  }, [keyword, status, page]);

  return (
    <section>
      <div className="row">
        <h1>Tickets</h1>
        <Link className="button" href="/tickets/new">
          Create a ticket
        </Link>
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
      {filtering && tickets.length === 0 ? null : <TicketTable tickets={tickets} />}
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
