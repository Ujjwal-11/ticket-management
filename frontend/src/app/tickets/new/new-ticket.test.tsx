import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { TicketForm } from "../../../components/TicketForm";
import { ApiError, createTicket } from "../../../services/ticket-api";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn() }),
}));

vi.mock("../../../services/ticket-api", async () => {
  const actual = await vi.importActual<typeof import("../../../services/ticket-api")>("../../../services/ticket-api");
  return { ...actual, createTicket: vi.fn() };
});

describe("create ticket", () => {
  beforeEach(() => {
    vi.mocked(createTicket).mockReset();
  });

  it("submits a valid ticket with Medium priority", async () => {
    const user = userEvent.setup();
    vi.mocked(createTicket).mockResolvedValue({
      id: 1,
      title: "Printer",
      description: "Jam",
      priority: "MEDIUM",
      status: "OPEN",
      assignee: null,
      createdAt: "2026-09-29T00:00:00Z",
      updatedAt: "2026-09-29T00:00:00Z",
      comments: [],
    });
    render(<TicketForm />);
    await user.type(screen.getByLabelText("Title"), "Printer");
    await user.type(screen.getByLabelText("Description"), "Jam");
    await user.click(screen.getByRole("button", { name: "Create" }));
    expect(createTicket).toHaveBeenCalledWith({
      title: "Printer",
      description: "Jam",
      priority: "MEDIUM",
      assignee: null,
    });
  });

  it("shows Title is required", async () => {
    const user = userEvent.setup();
    vi.mocked(createTicket).mockRejectedValue(
      new ApiError({
        type: "about:blank",
        title: "Bad Request",
        status: 400,
        detail: "Validation failed",
        instance: "/api/v1/tickets",
        errors: [{ field: "title", message: "Title is required" }],
      }),
    );
    render(<TicketForm />);
    await user.type(screen.getByLabelText("Description"), "Jam");
    await user.click(screen.getByRole("button", { name: "Create" }));
    expect(await screen.findByText("Title is required")).toBeInTheDocument();
  });
});
