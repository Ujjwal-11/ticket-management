package com.ticketmanagement.ticket.domain;

import com.ticketmanagement.ticket.service.IllegalTransitionException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TicketLifecycle {

  private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED =
      Map.of(
          TicketStatus.OPEN, EnumSet.of(TicketStatus.IN_PROGRESS),
          TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
          TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS),
          TicketStatus.CLOSED, EnumSet.of(TicketStatus.IN_PROGRESS));

  private TicketLifecycle() {}

  public static boolean isAllowed(TicketStatus from, TicketStatus to) {
    if (from == null || to == null) {
      return false;
    }
    return ALLOWED.getOrDefault(from, Set.of()).contains(to);
  }

  public static void requireTransition(TicketStatus from, TicketStatus to) {
    if (!isAllowed(from, to)) {
      String fromLabel = from == null ? "Unknown" : from.label();
      String toLabel = to == null ? "Unknown" : to.label();
      throw new IllegalTransitionException(
          "Cannot change status from " + fromLabel + " to " + toLabel + ".");
    }
  }
}
