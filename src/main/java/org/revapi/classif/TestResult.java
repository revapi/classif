package org.revapi.classif;

import java.util.function.Supplier;

public enum TestResult {
    PASSED, NOT_PASSED, DEFERRED;

    public static TestResult fromBoolean(boolean value) {
        return value ? PASSED : NOT_PASSED;
    }

    public boolean toBoolean(boolean undecidedValue) {
        switch (this) {
            case PASSED:
                return true;
            case NOT_PASSED:
                return false;
            case DEFERRED:
                return undecidedValue;
            default:
                throw new IllegalStateException("Unhandled TestResult value: " + this);
        }
    }

    public TestResult and(TestResult other) {
        switch (this) {
            case PASSED:
                return other;
            case NOT_PASSED:
                return this;
            case DEFERRED:
                return other == NOT_PASSED ? other : this;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

    public TestResult and(Supplier<TestResult> other) {
        switch (this) {
            case PASSED:
                return other.get();
            case NOT_PASSED:
                return this;
            case DEFERRED:
                TestResult res = other.get();
                return res == NOT_PASSED ? res : this;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

    public TestResult or(TestResult other) {
        switch (this) {
            case PASSED:
                return this;
            case NOT_PASSED:
                return other;
            case DEFERRED:
                return other == PASSED ? other : this;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

    public TestResult or(Supplier<TestResult> other) {
        switch (this) {
            case PASSED:
                return this;
            case NOT_PASSED:
                return other.get();
            case DEFERRED:
                TestResult res = other.get();
                return res == PASSED ? res : this;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

    public TestResult negate() {
        switch (this) {
            case PASSED:
                return NOT_PASSED;
            case NOT_PASSED:
                return PASSED;
            case DEFERRED:
                return DEFERRED;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

}
