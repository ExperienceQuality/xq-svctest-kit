package org.xq.testsdk.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Waits for a local test condition without allowing an unbounded retry loop. */
public final class BoundedPoller {
    private BoundedPoller() { }

    public static void until(Duration timeout, Duration interval, BooleanSupplier condition) {
        Objects.requireNonNull(timeout, "timeout");
        Objects.requireNonNull(interval, "interval");
        Objects.requireNonNull(condition, "condition");
        if (timeout.isNegative() || timeout.isZero() || interval.isNegative() || interval.isZero()) {
            throw new IllegalArgumentException("timeout and interval must be positive");
        }
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("polling was interrupted", exception);
            }
        }
        throw new AssertionError("condition did not pass within " + timeout);
    }
}
