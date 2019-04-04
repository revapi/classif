/*
 * Copyright 2018-2019 Lukas Krejci
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
