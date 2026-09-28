import { fieldErrors } from "../services/ticket-api";
import type { Priority } from "../services/ticket-labels";

export type FieldErrorMap = Record<string, string>;

export type InlineCreateDraft = {
  title: string;
  description: string;
  priority: Priority;
  assignee: string;
  fieldErrors: FieldErrorMap;
};

export type InlineEditDraft = {
  ticketId: number;
  title: string;
  priority: Priority;
  assignee: string;
  originalTitle: string;
  originalPriority: Priority;
  originalAssignee: string | null;
  fieldErrors: FieldErrorMap;
};

export function emptyCreateDraft(): InlineCreateDraft {
  return {
    title: "",
    description: "",
    priority: "MEDIUM",
    assignee: "",
    fieldErrors: {},
  };
}

export function fieldErrorsFromUnknown(error: unknown): FieldErrorMap {
  return fieldErrors(error);
}

export function fieldInvalidClass(field: string, errors: FieldErrorMap): string {
  return errors[field] ? "field-invalid" : "";
}

export function fieldMessage(field: string, errors: FieldErrorMap): string | undefined {
  return errors[field];
}
