package org.xq.testsdk.core;

import java.time.Duration;
import java.time.Instant;
import org.testng.ITestListener;
import org.testng.ITestResult;

/** Emits bounded, correlation-aware lifecycle diagnostics without recording test inputs. */
public final class TestNgLifecycleDiagnostics implements ITestListener {
    @Override public void onTestStart(ITestResult result) { result.setAttribute("xq.started-at", Instant.now()); }
    @Override public void onTestFailure(ITestResult result) { emit("failed", result); }
    @Override public void onTestSuccess(ITestResult result) { emit("passed", result); }

    private void emit(String outcome, ITestResult result) {
        Instant startedAt = (Instant) result.getAttribute("xq.started-at");
        long duration = startedAt == null ? 0 : Duration.between(startedAt, Instant.now()).toMillis();
        String ticket = System.getProperty("xq.hub.ticket", "unspecified");
        String pullRequest = System.getProperty("xq.pull-request", "unspecified");
        System.out.printf("xq-suite=%s ticket=%s pr=%s test=%s outcome=%s duration-ms=%d%n",
            System.getProperty("xq.medium.suite", "xq-svctest-kit-medium"), ticket, pullRequest,
            result.getMethod().getQualifiedName(), outcome, duration);
    }
}
