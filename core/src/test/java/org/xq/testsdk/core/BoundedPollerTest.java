package org.xq.testsdk.core;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class BoundedPollerTest {
    @Test
    public void returnsWhenConditionEventuallyPasses() {
        AtomicInteger attempts = new AtomicInteger();
        BoundedPoller.until(Duration.ofSeconds(1), Duration.ofMillis(1), () -> attempts.incrementAndGet() == 3);
        assertEquals(attempts.get(), 3);
    }
}
