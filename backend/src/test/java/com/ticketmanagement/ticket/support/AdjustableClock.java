package com.ticketmanagement.ticket.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class AdjustableClock extends Clock {

  private Instant instant;

  public AdjustableClock(Instant instant) {
    this.instant = instant;
  }

  public void plusSeconds(long seconds) {
    instant = instant.plusSeconds(seconds);
  }

  @Override
  public ZoneId getZone() {
    return ZoneOffset.UTC;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return this;
  }

  @Override
  public Instant instant() {
    return instant;
  }
}
