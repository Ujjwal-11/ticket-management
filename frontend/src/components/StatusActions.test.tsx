import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import type { Ticket } from "../services/ticket-api";
import { StatusActions } from "./StatusActions";

vi.mock("../services/ticket-api", async () => {
  const actual = await vi.importActual<typeof import("../services/ticket-api")>("../services/ticket-api");
  return { ...actual, transitionTicket: vi.fn() };
});

function ticket(status: Ticket["status"]): Ticket {
  return {
    id: 1,
    title: "T",
    description: "D",
    priority: "MEDIUM",
    status,
    assignee: null,
    createdAt: "2026-09-29T00:00:00Z",
    updatedAt: "2026-09-29T00:00:00Z",
    comments: [],
  };
}

describe("StatusActions", () => {
  it.each([
    ["OPEN", ["In Progress"]],
    ["IN_PROGRESS", ["Resolved", "Open"]],
    ["RESOLVED", ["Closed", "In Progress"]],
    ["CLOSED", ["In Progress"]],
  ] as const)("offers only the next statuses for %s", (status, labels) => {
    render(<StatusActions ticket={ticket(status)} onUpdated={vi.fn()} />);
    const buttons = screen.getAllByRole("button").map((button) => button.textContent);
    expect(buttons).toEqual([...labels]);
  });
});
