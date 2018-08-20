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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
                                if (e.getKey().isMatchAll() || e.getKey().isMatchAny()) {
                                    return e.getValue();
                                }

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
     * <p>
     * Note that this is a very expensive operation scaling with the <b>factorial</b> of the combined size of mandatory
     * and optional sets.
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

        int nofMatches = state.numberOfConcreteMatches() + state.matchAnys;

        Iterator<int[]> indices = state.potentiallyMatchingPermutations();

        TestResult bestResult = TestResult.fromBoolean(
                // if there are no permutations to try and there are no concrete matches to test
                !indices.hasNext() && state.numberOfConcreteMatches() == 0 &&
                        // and we can either match all
                        (state.matchAll 
                                // or the we have enough * to match all mandatory elements but not more than all
                                // the elements provided, then we can match immediately
                                || (state.matchAnys >= state.mandatories && state.matchAnys <= state.list.size()))
        );

        while (indices.hasNext()) {
            // to limit the number of tests that we need to do, the individualMatches map doesn't contain results
            // for * and **. This complicates things a little when computing the best result.

            int[] resultIndices = indices.next();
            Iterator<T> mit = state.concreteMatches().iterator();

            TestResult mandatoryResult = null;
            TestResult optionalResult = null;
            int mandatorySize = 0;
            int optionalSize = 0;
            for (int i = 0; i < resultIndices.length; ++i) {
                TestResult res;
                boolean resMandatory;
                boolean increaseCount;

                if (mit.hasNext()) {
                    Ctx<?> r = state.getResult(mit.next(), resultIndices[i]);
                    res = r.testResult;
                    resMandatory = r.mandatory;
                    increaseCount = true;
                } else {
                    Ctx<?> r = state.getUnevaluated(resultIndices[i]);
                    res = PASSED;
                    resMandatory = r.mandatory;
                    increaseCount = false;
                }

                if (resMandatory) {
                    mandatoryResult = mandatoryResult == null ? res : mandatoryResult.and(res);
                    if (increaseCount) {
                        mandatorySize++;
                    }
                } else {
                    optionalResult = optionalResult == null ? res : optionalResult.and(res);
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

    private static final class UnorderedMatchState<T extends Globbed, X> {
        final TestResult.BiPredicate<T, X> test;
        final List<Ctx<X>> list;
        final int matchAnys;
        final int mandatories;
        final boolean matchAll;
        final boolean nonMatchingTest;
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

            boolean hasNonMatchingTest = false;

            for (T m : matches) {
                if (m.isMatchAll()) {
                    matchAll = true;
                } else if (m.isMatchAny()) {
                    matchAnys++;
                } else {

                    // deep copy of the list
                    List<Ctx<X>> l = new ArrayList<>(list.size());
                    TestResult overall = NOT_PASSED;
                    for (Ctx<X> c : list) {
                        Ctx<X> copy = new Ctx<>(c.element, c.mandatory);
                        l.add(copy);
                        copy.testResult = test.test(m, c.element);
                        overall = overall.or(copy.testResult);
                    }

                    hasNonMatchingTest |= overall == NOT_PASSED;

                    individualResults.put(m, l);
                }
            }

            this.matchAnys = matchAnys;
            this.matchAll = matchAll;
            this.nonMatchingTest = hasNonMatchingTest;
        }

        boolean isDegenerate() {
            if (nonMatchingTest) {
                return true;
            }

            int nofMatches = individualResults.size() + matchAnys;

            // we know we can't match anything if a) the number of matches is larger that the number
            // of elements we were provided or b) there is not enough matches to match all the mandatory
            // elements. b) only applies if there is no matchAll match which supplements the lack of the
            // concrete tests.
            return nofMatches > list.size() || (!matchAll && nofMatches < mandatories);
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
            return individualResults.get(match).get(index);
        }

        Iterator<int[]> potentiallyMatchingPermutations() {
            if (individualResults.isEmpty()) {
                return new Iterator<int[]>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public int[] next() {
                        throw new NoSuchElementException();
                    }
                };
            }

            return new Iterator<int[]>() {
                final int[][] iterables;
                final int[] iterationPositions;

                {
                    Map<T, int[]> matchingIndicesPerMatch = individualResults.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                                List<Ctx<X>> ctxs = e.getValue();
                                return ctxs.stream()
                                        .filter(c -> c.testResult.toBoolean(true))
                                        .mapToInt(ctxs::indexOf)
                                        .toArray();
                            }));

                    iterables = matchingIndicesPerMatch.values().toArray(new int[matchingIndicesPerMatch.values().size()][]);

                    iterationPositions = new int[iterables.length];
                    iterationPositions[0] = -1;
                }

                HashSet<Integer> permutationChecker = new HashSet<>(iterationPositions.length);

                boolean consumed = true;

                @Override
                public boolean hasNext() {
                    if (consumed) {
                        do {
                            boolean canHaveNext = false;

                            for (int i = 0; i < iterationPositions.length; ++i) {
                                if (iterationPositions[i] < iterables[i].length - 1) {
                                    canHaveNext = true;
                                    break;
                                }
                            }

                            if (!canHaveNext) {
                                return false;
                            }

                            computeNext();
                        } while (!isValidPermutation());

                        consumed = false;
                    }

                    return true;
                }

                @Override
                public int[] next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }

                    int[] ret = new int[iterationPositions.length];
                    for (int i = 0; i < ret.length; ++i) {
                        ret[i] = iterables[i][iterationPositions[i]];
                    }

                    consumed = true;
                    return ret;
                }

                private void computeNext() {
                    if (!consumed) {
                        return;
                    }

                    for (int i = 0; i < iterationPositions.length; ++i) {
                        if (iterationPositions[i] == -1 || iterationPositions[i] < iterables[iterationPositions[i]].length - 1) {
                            iterationPositions[i]++;
                            for (int j = 0; j < i; ++j) {
                                iterationPositions[j] = 0;
                            }
                            break;
                        }
                    }
                }

                private boolean isValidPermutation() {
                    permutationChecker.clear();
                    for (int i = 0; i < iterationPositions.length; ++i) {
                        boolean newlyAdded = permutationChecker.add(iterables[i][iterationPositions[i]]);
                        if (!newlyAdded) {
                            return false;
                        }
                    }

                    return true;
                }
            };
        }
    }
}
