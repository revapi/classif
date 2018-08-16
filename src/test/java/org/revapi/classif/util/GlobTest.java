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
package org.revapi.classif.util;

import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;
import static org.revapi.classif.Tester.assertNotPassed;
import static org.revapi.classif.Tester.assertPassed;
import static org.revapi.classif.util.GlobTest.Matcher.ALL;
import static org.revapi.classif.util.GlobTest.Matcher.ANY;
import static org.revapi.classif.util.GlobTest.Matcher.TEST;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.revapi.classif.TestResult;

class GlobTest {

    @Test
    void testConcreteStepsRequirePresence() {
        assertPassed(test(array(PASSED, PASSED, PASSED), TEST, TEST, TEST));
        assertNotPassed(test(array(PASSED, PASSED, PASSED), TEST, TEST));
        assertNotPassed(test(array(PASSED, PASSED, PASSED), TEST, TEST, TEST, TEST));
    }

    @Test
    void testAnyRequiresPresence() {
        assertPassed(test(array(PASSED, PASSED, PASSED), TEST, ANY, TEST));
        assertPassed(test(array(PASSED, PASSED, PASSED), TEST, TEST, ANY));
        assertNotPassed(test(array(PASSED, PASSED, PASSED), TEST, ANY, TEST, ANY));
        assertNotPassed(test(array(PASSED, PASSED, PASSED), TEST, TEST, ANY, TEST));
    }

    @Test
    void testAllDoesntRequirePresence() {
        assertPassed(test(array(PASSED, PASSED), TEST, ALL));
        assertPassed(test(array(PASSED, PASSED, PASSED, PASSED), TEST, ALL));
        assertPassed(test(array(PASSED, PASSED), TEST, TEST, ALL));
        assertPassed(test(array(PASSED, PASSED), TEST, ALL, TEST));
        assertPassed(test(array(PASSED, PASSED), TEST, ALL, TEST, ALL));
        assertPassed(test(array(PASSED, PASSED), TEST, ALL, TEST, ALL, ALL));
        assertPassed(test(array(), ALL));
        assertPassed(test(array(PASSED, PASSED, PASSED, PASSED), ALL));
    }

    @Test
    void testAllWithNonPassingElements() {
        assertPassed(test(array(NOT_PASSED), ALL));
        assertPassed(test(array(PASSED, NOT_PASSED), ALL));
    }

    @Test
    void testUnordered() {
        assertPassed(testUnordered(array(PASSED, PASSED), TEST, TEST));

        //this should pass, because it should see the combination of ANY matching NOT_PASSED
        //and TEST matching PASSED.
        assertPassed(testUnordered(array(PASSED, PASSED, NOT_PASSED), TEST, ANY, TEST));

        // not passed, because there are more tests than elements available
        assertNotPassed(testUnordered(array(PASSED, NOT_PASSED), TEST, ANY, TEST));

        assertPassed(testUnordered(array(PASSED, PASSED), TEST, TEST, ALL));
        assertPassed(testUnordered(array(PASSED, PASSED), TEST, ALL));

        // ALL matches NOT_PASSED and TEST matches the single PASSED
        assertPassed(testUnordered(array(PASSED, NOT_PASSED, NOT_PASSED), TEST, ALL));

        assertPassed(testUnordered(array(NOT_PASSED, PASSED, NOT_PASSED), TEST, ANY, ALL));

        // not passed because there is not enough tests to match the elements
        assertNotPassed(testUnordered(array(PASSED, PASSED), TEST));
    }

    @Test
    void testUnorderedWithOptionals() {
        assertPassed(testUnorderedWithOptionals(array(PASSED, PASSED), array(NOT_PASSED), TEST, TEST));
        assertNotPassed(testUnorderedWithOptionals(array(PASSED, PASSED), array(NOT_PASSED), TEST, TEST, TEST));
        assertPassed(testUnorderedWithOptionals(array(PASSED, PASSED), array(NOT_PASSED), TEST, TEST, ANY));
        assertPassed(testUnorderedWithOptionals(array(PASSED, PASSED), array(NOT_PASSED), TEST, ANY, TEST));
        assertNotPassed(testUnorderedWithOptionals(array(PASSED, NOT_PASSED), array(NOT_PASSED), TEST, ANY, TEST));
        assertNotPassed(testUnorderedWithOptionals(array(PASSED, NOT_PASSED), array(NOT_PASSED), TEST, ALL, TEST));
        assertPassed(testUnorderedWithOptionals(array(PASSED, NOT_PASSED), array(NOT_PASSED, PASSED), TEST, ALL, TEST));
    }

    private TestResult[] array(TestResult... vals) {
        return vals;
    }

    private static TestResult test(TestResult[] testResults, Matcher... matchers) {
        return getGlob(matchers).test(test(testResults), nIndices(testResults.length));
    }

    private static TestResult testUnordered(TestResult[] testResults, Matcher... matchers) {
        return getGlob(matchers).testUnordered(test(testResults), nIndices(testResults.length));
    }

    private static TestResult testUnorderedWithOptionals(TestResult[] testResults, TestResult[] optionals, Matcher... matchers) {
        TestResult[] all = new TestResult[testResults.length + optionals.length];
        System.arraycopy(testResults, 0, all, 0, testResults.length);
        System.arraycopy(optionals, 0, all, testResults.length, optionals.length);

        return getGlob(matchers).testUnorderedWithOptionals(test(all), nIndices(testResults.length),
                nIndices(testResults.length, optionals.length));
    }

    private static <T> TestResult.BiPredicate<T, Integer> test(TestResult[] testResults) {
        return (__, i) -> testResults[i];
    }

    private static List<Integer> nIndices(int n) {
        return nIndices(0, n);
    }

    private static List<Integer> nIndices(int start, int n) {
        return Stream.iterate(start, i -> i + 1).limit(n).collect(Collectors.toList());
    }

    private static Glob<?> getGlob(Matcher... matchers) {
        class GlobMatcher implements Globbed {
            private final boolean any;
            private final boolean all;

            private GlobMatcher(boolean any, boolean all) {
                this.any = any;
                this.all = all;
            }

            @Override
            public boolean isMatchAny() {
                return any;
            }

            @Override
            public boolean isMatchAll() {
                return all;
            }
        }

        return new Glob<>(Stream.of(matchers).map(m -> {
            switch (m) {
                case TEST:
                    return new GlobMatcher(false, false);
                case ALL:
                    return new GlobMatcher(false, true);
                case ANY:
                    return new GlobMatcher(true, false);
                default:
                    throw new AssertionError();
            }
        }).collect(Collectors.toList()));
    }

    enum Matcher {
        TEST, ANY, ALL
    }
}
