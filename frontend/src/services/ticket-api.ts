import type { Priority, TicketStatus } from "./ticket-labels";

const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type FieldProblem = {
  field: string;
  message: string;
};

export type Problem = {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  errors?: FieldProblem[];
};

export class ApiError extends Error {
  problem: Problem;

  constructor(problem: Problem) {
    super(problem.detail);
    this.problem = problem;
  }
}

export type Comment = {
  id: number;
  authorName: string;
  body: string;
  createdAt: string;
};

export type TicketSummary = {
  id: number;
  title: string;
  priority: Priority;
  status: TicketStatus;
  assignee: string | null;
  createdAt: string;
  updatedAt: string;
};

export type Ticket = TicketSummary & {
  description: string;
  comments: Comment[];
};

export type TicketPage = {
  content: TicketSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type CreateTicketInput = {
  title: string;
  description: string;
  priority: Priority;
  assignee: string | null;
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    ...init,
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      ...init?.headers,
    },
  });
  if (!response.ok) {
    const problem = (await response.json()) as Problem;
    throw new ApiError(problem);
  }
  return (await response.json()) as T;
}

export function fieldErrors(error: unknown): Record<string, string> {
  if (!(error instanceof ApiError) || !error.problem.errors) {
    return {};
  }
  return Object.fromEntries(error.problem.errors.map((item) => [item.field, item.message]));
}

export function errorDetail(error: unknown): string {
  if (error instanceof ApiError) {
    return error.problem.detail;
  }
  return "Something went wrong";
}

export function createTicket(input: CreateTicketInput): Promise<Ticket> {
  return request<Ticket>("/api/v1/tickets", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function listTickets(query: { q?: string; status?: TicketStatus | ""; page?: number }): Promise<TicketPage> {
  const params = new URLSearchParams();
  if (query.q) {
    params.set("q", query.q);
  }
  if (query.status) {
    params.set("status", query.status);
  }
  params.set("page", String(query.page ?? 0));
  return request<TicketPage>(`/api/v1/tickets?${params.toString()}`);
}

export function getTicket(id: string): Promise<Ticket> {
  return request<Ticket>(`/api/v1/tickets/${id}`);
}

export function updateTicket(
  id: number,
  input: { title?: string; description?: string; priority?: Priority; assignee?: string | null },
): Promise<Ticket> {
  return request<Ticket>(`/api/v1/tickets/${id}`, {
    method: "PATCH",
    body: JSON.stringify(input),
  });
}

export function transitionTicket(id: number, status: TicketStatus): Promise<Ticket> {
  return request<Ticket>(`/api/v1/tickets/${id}/transitions`, {
    method: "POST",
    body: JSON.stringify({ status }),
  });
}

export function addComment(id: number, authorName: string, body: string): Promise<Comment> {
  return request<Comment>(`/api/v1/tickets/${id}/comments`, {
    method: "POST",
    body: JSON.stringify({ authorName, body }),
  });
}
