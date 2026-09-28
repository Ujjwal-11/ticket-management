"use client";

import { useState } from "react";
import { errorDetail, transitionTicket, type Ticket } from "../services/ticket-api";
import { NEXT_STATUSES, statusLabel, type TicketStatus } from "../services/ticket-labels";

export function StatusActions({ ticket, onUpdated }: { ticket: Ticket; onUpdated: (ticket: Ticket) => void }) {
  const [detail, setDetail] = useState("");

  async function move(status: TicketStatus) {
    setDetail("");
    try {
      onUpdated(await transitionTicket(ticket.id, status));
    } catch (error) {
      setDetail(errorDetail(error));
    }
  }

  return (
    <div className="row">
      {NEXT_STATUSES[ticket.status].map((status) => (
        <button key={status} type="button" onClick={() => move(status)}>
          {statusLabel(status)}
        </button>
      ))}
      {detail ? <p className="error">{detail}</p> : null}
    </div>
  );
}
