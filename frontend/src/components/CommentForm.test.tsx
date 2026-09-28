import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { addComment, ApiError } from "../services/ticket-api";
import { CommentForm } from "./CommentForm";

vi.mock("../services/ticket-api", async () => {
  const actual = await vi.importActual<typeof import("../services/ticket-api")>("../services/ticket-api");
  return { ...actual, addComment: vi.fn() };
});

describe("CommentForm", () => {
  it("shows Comment is required", async () => {
    const user = userEvent.setup();
    vi.mocked(addComment).mockRejectedValue(
      new ApiError({
        type: "about:blank",
        title: "Bad Request",
        status: 400,
        detail: "Validation failed",
        instance: "/api/v1/tickets/1/comments",
        errors: [{ field: "body", message: "Comment is required" }],
      }),
    );
    render(<CommentForm ticketId={1} status="OPEN" onCreated={vi.fn()} />);
    await user.type(screen.getByLabelText("Author name"), "Ada");
    await user.click(screen.getByRole("button", { name: "Add comment" }));
    expect(await screen.findByText("Comment is required")).toBeInTheDocument();
  });

  it("shows Author name is required", async () => {
    const user = userEvent.setup();
    vi.mocked(addComment).mockRejectedValue(
      new ApiError({
        type: "about:blank",
        title: "Bad Request",
        status: 400,
        detail: "Validation failed",
        instance: "/api/v1/tickets/1/comments",
        errors: [{ field: "authorName", message: "Author name is required" }],
      }),
    );
    render(<CommentForm ticketId={1} status="OPEN" onCreated={vi.fn()} />);
    await user.type(screen.getByLabelText("Comment"), "Hello");
    await user.click(screen.getByRole("button", { name: "Add comment" }));
    expect(await screen.findByText("Author name is required")).toBeInTheDocument();
  });

  it("shows the closed ticket message", () => {
    render(<CommentForm ticketId={1} status="CLOSED" onCreated={vi.fn()} />);
    expect(screen.getByText("Closed tickets cannot be changed. Reopen the ticket first.")).toBeInTheDocument();
  });
});
