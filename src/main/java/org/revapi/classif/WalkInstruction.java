package org.revapi.classif;

import static org.revapi.classif.TestResult.DEFERRED;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;

/**
 * The {@link MatchingProgress} assumes the caller supplies it with a tree of java model elements in a depth first
 * search manner. While doing so, it tells the caller the result of the testing of the elements as they are supplied
 * but also instructs the caller on how to proceed with the depth-first-search walk.
 */
public final class WalkInstruction {
    private static final WalkInstruction DESCEND_PASSED = new WalkInstruction(true, PASSED);
    private static final WalkInstruction DESCEND_NOT_PASSED = new WalkInstruction(true, NOT_PASSED);
    private static final WalkInstruction DESCEND_DEFERRED = new WalkInstruction(true, DEFERRED);
    private static final WalkInstruction NO_DESCEND_PASSED = new WalkInstruction(false, PASSED);
    private static final WalkInstruction NO_DESCEND_NOT_PASSED = new WalkInstruction(false, NOT_PASSED);
    private static final WalkInstruction NO_DESCEND_DEFERRED = new WalkInstruction(false, DEFERRED);

    private final boolean descend;
    private final TestResult testResult;

    public static WalkInstruction of(boolean descend, TestResult testResult) {
        switch (testResult) {
            case PASSED:
                return descend ? DESCEND_PASSED : NO_DESCEND_PASSED;
            case NOT_PASSED:
                return descend ? DESCEND_NOT_PASSED : NO_DESCEND_NOT_PASSED;
            case DEFERRED:
                return descend ? DESCEND_DEFERRED : NO_DESCEND_DEFERRED;
            default:
                throw new IllegalArgumentException("Unsupported test result value: " + testResult);
        }
    }

    private WalkInstruction(boolean descend, TestResult testResult) {
        this.descend = descend;
        this.testResult = testResult;
    }

    public boolean isDescend() {
        return descend;
    }

    public TestResult getTestResult() {
        return testResult;
    }
}
