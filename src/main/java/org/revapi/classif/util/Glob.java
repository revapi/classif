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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.revapi.classif.TestResult;

public final class Glob<T extends Globbed> {

    private final MatchState<T> startState;
    private final List<T> matches;

    /**
     * This is sort of like an NFA for regexes, only our matches are not that complex. We only need to deal with
     * {@code **} which adds a little bit of complexity to the resolution. If it weren't for {@code **} we wouldn't
     * even need this class at all.
     *
     * @param matches the matches to create the match automaton from
     */
    public Glob(List<T> matches) {
        this.matches = matches;

        MatchState<T> start = new MatchState<>();
        List<MatchState<T>> currents = new ArrayList<>(2);
        currents.add(start);

        for (T m : matches) {
            if (m.isMatchAll()) {
                // this matches 0 or more elements
                MatchState<T> next = new MatchState<>();
                currents.forEach(ms -> ms.nexts.put(m, next));

                // so that we can loop
                next.nexts.put(m, next);

                // don't clear the currents here so that we model the "0" matches
                currents.add(next);
            } else {
                // otherwise the simple case - we always proceed to the next match.
                MatchState<T> next = new MatchState<>();
                currents.forEach(ms -> ms.nexts.put(m, next));

                currents.clear();
                currents.add(next);
            }
        }

        currents.forEach(ms -> ms.terminal = true);

        startState = start;
    }

    public <X> TestResult test(TestResult.BiPredicate<T, X> test, Iterable<X> elements) {
        List<MatchState<T>> branches = new ArrayList<>(2);
        branches.add(startState);
        boolean[] isDeferred = {false};

        for (X t : elements) {
            List<MatchState<T>> next = branches.stream()
                    .flatMap(ms -> ms.nexts.entrySet().stream()
                            .map(e -> {
                                switch (test.test(e.getKey(), t)) {
                                    case DEFERRED:
                                        isDeferred[0] = true;
                                        // intentional fallthrough
                                    case PASSED:
                                        return e.getValue();
                                    default:
                                        //noinspection ReturnOfNull
                                        return null;
                                }
                            })
                            .filter(Objects::nonNull))
                    .collect(toList());

            if (isDeferred[0]) {
                break;
            }

            branches.clear();
            branches.addAll(next);
        }

        return isDeferred[0]
                ? TestResult.DEFERRED
                : TestResult.fromBoolean(branches.stream().anyMatch(ms -> ms.terminal));
    }

    public <X> TestResult testUnordered(TestResult.BiPredicate<T, X> test, Iterable<X> elements) {
        return testUnorderedWithOptionals(test, elements, emptyList());
    }

    /**
     * Makes sure that this Glob matches the provided sets of mandatory and optional elements.
     * <p>
     * Each of the elements from the mandatory set must be matched by exactly one of the matches in this glob.
     * In addition, if there are more matches in this glob than there are elements in the mandatory set, each such
     * "superfluous" match must match exactly one element from the optional set.
     * <p>
     * The {@link Globbed#isMatchAll() "all"} match only matches the "rest" of the elements, once all of the other
     * matches in this glob are taken into the account.
     *
     * @param test      the method to perform the test given a match from this glob and an element from either the mandatory
     *                  or optional set
     * @param mandatory the set of the elements that must be matched by the matches in this glob
     * @param optional  the set of elements that must be matched by matches from this glob that don't match any mandatory
     *                  element
     * @param <X>       the type of the elements in the mandatory and optional sets
     * @return the test result
     */
    public <X> TestResult testUnorderedWithOptionals(TestResult.BiPredicate<T, X> test, Iterable<X> mandatory,
            Iterable<X> optional) {
        
        UnorderedMatchState<T, X> state = new UnorderedMatchState<>(test, mandatory, optional, matches);

        // quickly deal with non-sensical corner cases
        if (state.isDegenerate()) {
            return NOT_PASSED;
        }

        TestResult bestResult = NOT_PASSED;

        int nofMatches = state.numberOfConcreteMatches() + state.matchAnys;

        // to limit the number of tests that we need to do, the individualMatches map doesn't contain results
        // for * and **. This complicates things a little when computing the best result.

        Permutations permutations = new Permutations(state.getListSize());
        while (permutations.hasNext()) {
            int[] resultIndices = permutations.next();
            Iterator<T> mit = state.concreteMatches().iterator();

            TestResult mandatoryResult = null;
            TestResult optionalResult = null;
            int mandatorySize = 0;
            int optionalSize = 0;
            for (int i = 0; i < resultIndices.length; ++i) {
                Ctx<?> res;
                boolean increaseCount;

                if (mit.hasNext()) {
                    res = state.getResult(mit.next(), resultIndices[i]);
                    increaseCount = true;
                } else {
                    res = state.getUnevaluated(resultIndices[i]);
                    res = new Ctx<>(res.element, res.mandatory, PASSED);
                    increaseCount = false;
                }

                if (res.mandatory) {
                    mandatoryResult = mandatoryResult == null ? res.testResult : mandatoryResult.and(res.testResult);
                    if (increaseCount) {
                        mandatorySize++;
                    }
                } else {
                    optionalResult = optionalResult == null ? res.testResult : optionalResult.and(res.testResult);
                    if (increaseCount) {
                        optionalSize++;
                    }
                }
            }

            if (mandatoryResult == null) {
                mandatoryResult = NOT_PASSED;
            }

            if (optionalResult == null) {
                optionalResult = TestResult.fromBoolean(optionalSize == 0);
            }

            TestResult permutationResult;
            if (state.matchAll) {
                permutationResult = mandatoryResult.and(optionalResult);
            } else if (nofMatches < mandatorySize) {
                permutationResult = NOT_PASSED;
            } else if (nofMatches == mandatorySize) {
                permutationResult = mandatoryResult;
            } else {
                // nofMatches > mandatorySize
                permutationResult = mandatoryResult.and(optionalResult);
            }

            bestResult = bestResult.or(permutationResult);

            if (bestResult == PASSED) {
                // can't get better than this
                break;
            }
        }

        return bestResult;
    }

    private static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }

    private static final class MatchState<T extends Globbed> {
        private final Map<T, MatchState<T>> nexts = new IdentityHashMap<>(4);
        private boolean terminal;
    }

    private static class Ctx<X> {
        final boolean mandatory;
        final X element;
        TestResult testResult;

        Ctx(X element, boolean mandatory) {
            this(element, mandatory, null);
        }

        Ctx(X element, boolean mandatory, TestResult testResult) {
            this.mandatory = mandatory;
            this.element = element;
            this.testResult = testResult;
        }
    }

    private static class UnorderedMatchState<T extends Globbed, X> {
        final TestResult.BiPredicate<T, X> test;
        final List<Ctx<X>> list;
        final int matchAnys;
        final int mandatories;
        final boolean matchAll;
        final Map<T, List<Ctx<X>>> individualResults;

        UnorderedMatchState(TestResult.BiPredicate<T, X> test, Iterable<X> mandatories, Iterable<X> optionals,
                List<T> matches) {
            this.test = test;
            int[] tmp = new int[1];
            this.list = Stream.concat(
                    stream(mandatories).map(e -> {
                        tmp[0]++;
                        return new Ctx<>(e, true);
                    }),
                    stream(optionals).map(e -> new Ctx<>(e, false))
            ).collect(toList());

            this.mandatories = tmp[0];

            boolean matchAll = false;
            int matchAnys = 0;
            individualResults = new HashMap<>();

            for (T m : matches) {
                if (m.isMatchAll()) {
                    matchAll = true;
                } else if (m.isMatchAny()) {
                    matchAnys++;
                } else {
                    // deep copy of the list
                    List<Ctx<X>> l = new ArrayList<>(list.size());
                    for (Ctx<X> c : list) {
                        l.add(new Ctx<>(c.element, c.mandatory));
                    }

                    individualResults.put(m, l);
                }
            }

            this.matchAnys = matchAnys;
            this.matchAll = matchAll;
        }

        boolean isDegenerate() {
            int nofMatches = individualResults.size() + matchAnys;

            // we know we can't match anything if a) the number of matches is larger that the number
            // of elements we were provided or b) there is not enough matches to match all the mandatory
            // elements. b) only applies if there is no matchAll match which supplements the lack of the
            // concrete tests.
            return nofMatches > list.size() || (!matchAll && nofMatches < mandatories);
        }

        int getListSize() {
            return list.size();
        }

        int numberOfConcreteMatches() {
            return individualResults.size();
        }

        Set<T> concreteMatches() {
            return individualResults.keySet();
        }

        Ctx<X> getUnevaluated(int index) {
            return list.get(index);
        }

        Ctx<X> getResult(T match, int index) {
            Ctx<X> ret = individualResults.get(match).get(index);

            if (ret.testResult == null) {
                ret.testResult = test.test(match, ret.element);
            }

            return ret;
        }

    }

    private static final class Permutations implements Iterator<int[]> {
        private final int[] currentIndices;
        private boolean hasNext;
        private boolean first;

        Permutations(int size) {
            currentIndices = IntStream.iterate(0, i -> i + 1).limit(size).toArray();
            hasNext = size > 0;
            first = true;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public int[] next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }

            computeNextIndices();

            return currentIndices;
        }

        // heavily inspired by https://stackoverflow.com/a/14444037/1969945
        private void computeNextIndices() {
            if (first) {
                first = false;
                return;
            }

            hasNext = false;
            for (int tail = currentIndices.length - 1; tail > 0; tail--) {
                if (currentIndices[tail - 1] < currentIndices[tail]) { // still increasing

                    // find last element which does not exceed ind[tail-1]
                    int s = currentIndices.length - 1;
                    while (currentIndices[tail - 1] >= currentIndices[s]) {
                        s--;
                    }

                    swap(currentIndices, tail - 1, s);

                    // reverse order of elements in the tail
                    for (int i = tail, j = currentIndices.length - 1; i < j; i++, j--) {
                        swap(currentIndices, i, j);
                    }

                    hasNext = true;
                    break;
                }

            }
        }

        private static void swap(int[] arr, int i, int j) {
            int t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        }
    }
}
