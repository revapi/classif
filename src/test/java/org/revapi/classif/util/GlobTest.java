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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.revapi.classif.util.GlobTest.Matcher.ALL;
import static org.revapi.classif.util.GlobTest.Matcher.ANY;
import static org.revapi.classif.util.GlobTest.Matcher.TEST;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.revapi.classif.match.util.Glob;
import org.revapi.classif.match.util.Globbed;

class GlobTest {

    @Test
    void testConcreteStepsRequirePresence() {
        assertTrue(test(array(true, true, true), TEST, TEST, TEST));
        assertFalse(test(array(true, true, true), TEST, TEST));
        assertFalse(test(array(true, true, true), TEST, TEST, TEST, TEST));
    }

    @Test
    void testAnyRequiresPresence() {
        assertTrue(test(array(true, true, true), TEST, ANY, TEST));
        assertTrue(test(array(true, true, true), TEST, TEST, ANY));
        assertFalse(test(array(true, true, true), TEST, ANY, TEST, ANY));
        assertFalse(test(array(true, true, true), TEST, TEST, ANY, TEST));
    }

    @Test
    void testAllDoesntRequirePresence() {
        assertTrue(test(array(true, true), TEST, ALL));
        assertTrue(test(array(true, true, true, true), TEST, ALL));
        assertTrue(test(array(true, true), TEST, TEST, ALL));
        assertTrue(test(array(true, true), TEST, ALL, TEST));
        assertTrue(test(array(true, true), TEST, ALL, TEST, ALL));
        assertTrue(test(array(true, true), TEST, ALL, TEST, ALL, ALL));
        assertTrue(test(array(), ALL));
        assertTrue(test(array(true, true, true, true), ALL));
    }

    private boolean[] array(boolean... vals) {
        return vals;
    }

    private static boolean test(boolean[] testResults, Matcher... matchers) {
        return getGlob(matchers).test(test(testResults), nIndices(testResults.length));
    }

    private static boolean testUnordered(boolean[] testResults, Matcher... matchers) {
        return getGlob(matchers).testUnordered(test(testResults), nIndices(testResults.length));
    }

    private static boolean testUnorderedWithOptionals(boolean[] testResults, boolean[] optionals, Matcher... matchers) {
        return getGlob(matchers).testUnorderedWithOptionals(test(testResults), nIndices(testResults.length),
                nIndices(optionals.length));
    }

    private static <T> BiPredicate<T, Integer> test(boolean[] testResults) {
        return (__, i) -> testResults[i];
    }

    private static List<Integer> nIndices(int n) {
        return Stream.iterate(0, i -> i + 1).limit(n).collect(Collectors.toList());
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
