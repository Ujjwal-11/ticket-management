"use client";

import { FormEvent, useState } from "react";
import type { TicketStatus } from "../services/ticket-labels";

type Props = {
  keyword: string;
  status: TicketStatus | "";
  resultCount: number;
  filtering: boolean;
  onApply: (keyword: string, status: TicketStatus | "") => void;
  onClear: () => void;
};

export function QueueFilters({ keyword, status, resultCount, filtering, onApply, onClear }: Props) {
  const [draftKeyword, setDraftKeyword] = useState(keyword);
  const [draftStatus, setDraftStatus] = useState<TicketStatus | "">(status);

  function submit(event: FormEvent) {
    event.preventDefault();
    onApply(draftKeyword, draftStatus);
  }

  return (
    <form className="panel" onSubmit={submit}>
      <div className="row">
        <label>
          Keyword
          <input value={draftKeyword} onChange={(event) => setDraftKeyword(event.target.value)} />
        </label>
        <label>
          Status
          <select
            value={draftStatus}
            onChange={(event) => setDraftStatus(event.target.value as TicketStatus | "")}
          >
            <option value="">All</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
          </select>
        </label>
        <button type="submit">Apply</button>
        <button
          type="button"
          onClick={() => {
            setDraftKeyword("");
            setDraftStatus("");
            onClear();
          }}
        >
          Clear
        </button>
      </div>
      {filtering && resultCount === 0 ? <p>No tickets match.</p> : null}
    </form>
  );
}
