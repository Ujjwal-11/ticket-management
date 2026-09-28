import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { addComment, getTicket, type Ticket } from "../services/ticket-api";
import { TicketDetail } from "./TicketDetail";

vi.mock("../services/ticket-api", async () => {
  const actual = await vi.importActual<typeof import("../services/ticket-api")>("../services/ticket-api");
  return { ...actual, addComment: vi.fn(), getTicket: vi.fn() };
});

const ticket: Ticket = {
  id: 4,
  title: "Printer",
  description: "Paper jam",
  priority: "LOW",
  status: "OPEN",
  assignee: "Ada",
  createdAt: "2026-09-29T00:00:00Z",
  updatedAt: "2026-09-29T01:00:00Z",
  comments: [
    { id: 1, authorName: "Ada", body: "Earlier", createdAt: "2026-09-29T00:10:00Z" },
    { id: 2, authorName: "Bea", body: "Later", createdAt: "2026-09-29T00:20:00Z" },
  ],
};

describe("TicketDetail", () => {
  it("shows fields and comments in order", () => {
    render(<TicketDetail ticket={ticket} />);
    expect(screen.getByText("Paper jam", { selector: "p" })).toBeInTheDocument();
    expect(screen.getByText("Assignee Ada")).toBeInTheDocument();
    const items = screen.getAllByRole("listitem").map((item) => item.textContent);
    expect(items[0]).toContain("Earlier");
    expect(items[1]).toContain("Later");
  });

  it("reloads the ticket after a comment so Updated moves forward", async () => {
    const user = userEvent.setup();
    vi.mocked(addComment).mockResolvedValue({
      id: 3,
      authorName: "Bea",
      body: "New note",
      createdAt: "2026-09-29T02:00:00Z",
    });
    vi.mocked(getTicket).mockResolvedValue({
      ...ticket,
      updatedAt: "2026-09-29T02:00:00Z",
      comments: [...ticket.comments, { id: 3, authorName: "Bea", body: "New note", createdAt: "2026-09-29T02:00:00Z" }],
    });
    render(<TicketDetail ticket={ticket} />);
    await user.type(screen.getByLabelText("Author name"), "Bea");
    await user.type(screen.getByLabelText("Comment"), "New note");
    await user.click(screen.getByRole("button", { name: "Add comment" }));
    await waitFor(() => expect(getTicket).toHaveBeenCalledWith("4"));
    expect(screen.getByText(/Bea: New note/)).toBeInTheDocument();
  });

  it("shows a missing ticket without an editor", () => {
    render(<TicketDetail notFound />);
    expect(screen.getByText("Ticket not found")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Save" })).not.toBeInTheDocument();
  });
});
