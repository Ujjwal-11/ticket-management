import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import type { TicketSummary } from "../services/ticket-api";
import { emptyCreateDraft } from "./inline-queue-state";
import { TicketTable } from "./TicketTable";

const ticket: TicketSummary = {
  id: 7,
  title: "Printer",
  priority: "HIGH",
  status: "OPEN",
  assignee: null,
  createdAt: "2026-09-29T00:00:00Z",
  updatedAt: "2026-09-29T01:00:00Z",
};

const baseProps = {
  createActive: false,
  createDraft: emptyCreateDraft(),
  onCreateDraftChange: vi.fn(),
  onCreateSuccess: vi.fn(),
  onCreateCancel: vi.fn(),
  editingTicketId: null,
  editDraft: null,
  onEditDraftChange: vi.fn(),
  onEditSuccess: vi.fn(),
  onEditCancel: vi.fn(),
  onRequestEdit: vi.fn(),
};

describe("TicketTable", () => {
  it("shows the empty queue", () => {
    render(<TicketTable tickets={[]} {...baseProps} />);
    expect(screen.getByText("There are no tickets.")).toBeInTheDocument();
  });

  it("shows queue columns", () => {
    render(<TicketTable tickets={[ticket]} {...baseProps} />);
    expect(screen.getByText("Number")).toBeInTheDocument();
    expect(screen.getByText("Printer")).toBeInTheDocument();
    expect(screen.getByText("Open")).toBeInTheDocument();
    expect(screen.getByText("High")).toBeInTheDocument();
    expect(screen.getByText("Unassigned")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Edit ticket" })).toBeInTheDocument();
  });
});
