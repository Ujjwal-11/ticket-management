"use client";

import { useEffect, useState } from "react";
import { ApiError, getTicket, type Ticket } from "../services/ticket-api";
import { TicketDetail } from "./TicketDetail";

export function TicketScreen({ id }: { id: string }) {
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let active = true;
    getTicket(id)
      .then((loaded) => {
        if (active) {
          setTicket(loaded);
        }
      })
      .catch((error: unknown) => {
        if (active && error instanceof ApiError && error.problem.status === 404) {
          setNotFound(true);
        }
      });
    return () => {
      active = false;
    };
  }, [id]);

  if (notFound) {
    return <TicketDetail notFound />;
  }
  if (!ticket) {
    return <p>Loading ticket</p>;
  }
  return <TicketDetail ticket={ticket} />;
}
