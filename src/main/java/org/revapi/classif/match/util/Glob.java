package org.revapi.classif.match.util;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public <X> boolean test(BiPredicate<T, X> test, Iterable<X> elements) {
        List<MatchState<T>> branches = new ArrayList<>(2);
        branches.add(startState);

        elements.forEach(t -> {
            List<MatchState<T>> next = branches.stream()
                    .flatMap(ms -> ms.nexts.entrySet().stream()
                            .map(e -> test.test(e.getKey(), t) ? e.getValue() : null)
                            .filter(Objects::nonNull))
                    .collect(toList());

            branches.clear();
            branches.addAll(next);
        });

        return branches.stream().anyMatch(ms -> ms.terminal);
    }

    public <X> boolean testUnordered(BiPredicate<T, X> test, Iterable<X> elements) {
        return testUnorderedWithOptionals(test, elements, emptyList());
    }

    public <X> boolean testUnorderedWithOptionals(BiPredicate<T, X> test, Iterable<X> mandatory, Iterable<X> optional) {
        return stream(mandatory)
                .reduce(true,
                        (res, next) -> res && matches.stream().anyMatch(m ->
                                test.test(m, next) || stream(optional).anyMatch(x -> test.test(m, x))),
                        Boolean::logicalAnd);
    }

    private static final class MatchState<T extends Globbed> {
        private final Map<T, MatchState<T>> nexts = new IdentityHashMap<>(4);
        private boolean terminal;
    }


    private static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }
}
