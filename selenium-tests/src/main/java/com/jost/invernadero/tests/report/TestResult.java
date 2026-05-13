package com.jost.invernadero.tests.report;

import java.util.Objects;
import java.util.Optional;

public class TestResult {

    private final String name;
    private final Status status;
    private final long durationMillis;
    private final String errorMessage;

    public TestResult(String name, Status status, long durationMillis, String errorMessage) {
        this.name = Objects.requireNonNull(name, "name");
        this.status = Objects.requireNonNull(status, "status");
        this.durationMillis = durationMillis;
        this.errorMessage = errorMessage;
    }

    public static TestResult passed(String name, long durationMillis) {
        return new TestResult(name, Status.PASSED, durationMillis, null);
    }

    public static TestResult failed(String name, long durationMillis, String errorMessage) {
        return new TestResult(name, Status.FAILED, durationMillis, errorMessage);
    }

    public static TestResult error(String name, long durationMillis, String errorMessage) {
        return new TestResult(name, Status.ERROR, durationMillis, errorMessage);
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public boolean isPassed() {
        return status == Status.PASSED;
    }

    public enum Status {
        PASSED,
        FAILED,
        ERROR
    }
}
