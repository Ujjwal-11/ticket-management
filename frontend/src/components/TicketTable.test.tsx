import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import type { TicketSummary } from "../services/ticket-api";
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

describe("TicketTable", () => {
  it("shows the empty queue", () => {
    render(<TicketTable tickets={[]} />);
    expect(screen.getByText("There are no tickets.")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Create a ticket" })).toBeInTheDocument();
  });

  it("shows queue columns", () => {
    render(<TicketTable tickets={[ticket]} />);
    expect(screen.getByText("Number")).toBeInTheDocument();
    expect(screen.getByText("Printer")).toBeInTheDocument();
    expect(screen.getByText("Open")).toBeInTheDocument();
    expect(screen.getByText("High")).toBeInTheDocument();
    expect(screen.getByText("Unassigned")).toBeInTheDocument();
  });
});
