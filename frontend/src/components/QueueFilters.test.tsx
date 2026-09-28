import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { QueueFilters } from "./QueueFilters";

describe("QueueFilters", () => {
  it("applies keyword and status, shows no matches, and clears", async () => {
    const user = userEvent.setup();
    const onApply = vi.fn();
    const onClear = vi.fn();
    const { rerender } = render(
      <QueueFilters keyword="" status="" resultCount={0} filtering={false} onApply={onApply} onClear={onClear} />,
    );
    await user.type(screen.getByLabelText("Keyword"), "login");
    await user.selectOptions(screen.getByLabelText("Status"), "OPEN");
    await user.click(screen.getByRole("button", { name: "Apply" }));
    expect(onApply).toHaveBeenCalledWith("login", "OPEN");

    rerender(
      <QueueFilters keyword="login" status="OPEN" resultCount={0} filtering onApply={onApply} onClear={onClear} />,
    );
    expect(screen.getByText("No tickets match.")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Clear" }));
    expect(onClear).toHaveBeenCalled();
  });
});
