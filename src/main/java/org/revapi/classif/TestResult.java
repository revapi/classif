/*
 * Copyright 2018 Lukas Krejci
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

    public TestResult define(boolean undecidedValue) {
        return fromBoolean(toBoolean(undecidedValue));
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
