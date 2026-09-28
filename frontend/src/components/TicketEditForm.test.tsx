import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import type { Ticket } from "../services/ticket-api";
import { updateTicket } from "../services/ticket-api";
import { TicketEditForm } from "./TicketEditForm";

vi.mock("../services/ticket-api", async () => {
  const actual = await vi.importActual<typeof import("../services/ticket-api")>("../services/ticket-api");
  return { ...actual, updateTicket: vi.fn() };
});

const openTicket: Ticket = {
  id: 3,
  title: "Old",
  description: "Body",
  priority: "LOW",
  status: "OPEN",
  assignee: "Ada",
  createdAt: "2026-09-29T00:00:00Z",
  updatedAt: "2026-09-29T00:00:00Z",
  comments: [],
};

describe("TicketEditForm", () => {
  it("saves fields and can clear the assignee", async () => {
    const user = userEvent.setup();
    vi.mocked(updateTicket).mockResolvedValue({ ...openTicket, title: "New", assignee: null });
    render(<TicketEditForm ticket={openTicket} onUpdated={vi.fn()} />);
    await user.clear(screen.getByLabelText("Title"));
    await user.type(screen.getByLabelText("Title"), "New");
    await user.clear(screen.getByLabelText("Assignee"));
    await user.click(screen.getByRole("button", { name: "Save" }));
    expect(updateTicket).toHaveBeenCalledWith(3, {
      title: "New",
      description: "Body",
      priority: "LOW",
      assignee: null,
    });
    expect(updateTicket).not.toHaveBeenCalledWith(expect.objectContaining({ status: expect.anything() }));
  });

  it("shows the closed ticket message", () => {
    render(<TicketEditForm ticket={{ ...openTicket, status: "CLOSED" }} onUpdated={vi.fn()} />);
    expect(screen.getByText("Closed tickets cannot be changed. Reopen the ticket first.")).toBeInTheDocument();
  });
});
