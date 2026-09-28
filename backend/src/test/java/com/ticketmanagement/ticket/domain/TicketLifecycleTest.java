package com.ticketmanagement.ticket.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketmanagement.ticket.service.IllegalTransitionException;
import org.junit.jupiter.api.Test;

class TicketLifecycleTest {

  @Test
  void allowsOnlyTheSixSpecTransitions() {
    for (TicketStatus from : TicketStatus.values()) {
      for (TicketStatus to : TicketStatus.values()) {
        boolean allowed = TicketLifecycle.isAllowed(from, to);
        boolean expected =
            (from == TicketStatus.OPEN && to == TicketStatus.IN_PROGRESS)
                || (from == TicketStatus.IN_PROGRESS && to == TicketStatus.RESOLVED)
                || (from == TicketStatus.IN_PROGRESS && to == TicketStatus.OPEN)
                || (from == TicketStatus.RESOLVED && to == TicketStatus.CLOSED)
                || (from == TicketStatus.RESOLVED && to == TicketStatus.IN_PROGRESS)
                || (from == TicketStatus.CLOSED && to == TicketStatus.IN_PROGRESS);
        assertThat(allowed).as("%s -> %s", from, to).isEqualTo(expected);
        if (expected) {
          TicketLifecycle.requireTransition(from, to);
        } else {
          assertThatThrownBy(() -> TicketLifecycle.requireTransition(from, to))
              .isInstanceOf(IllegalTransitionException.class)
              .hasMessage("Cannot change status from " + from.label() + " to " + to.label() + ".");
        }
      }
    }
  }
}
