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
package org.revapi.classif.match.instance;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.revapi.classif.match.MatchContext;

public final class TypeParametersMatch extends TypeInstanceMatch {
    private final MatchState matchAutomaton;

    public TypeParametersMatch(List<TypeParameterMatch> matches) {
        this.matchAutomaton = MatchState.createAutomaton(matches);
    }

    @Override
    protected <M> boolean testIntersection(IntersectionType t, MatchContext<M> matchContext) {
        return t.getBounds().stream().anyMatch(b -> testInstance(b, matchContext));
    }

    @Override
    protected <M> boolean testDeclared(DeclaredType type, MatchContext<M> matchContext) {
        List<? extends TypeMirror> tps = type.getTypeArguments();
        List<MatchState> branches = new ArrayList<>(2);
        branches.add(matchAutomaton);

        tps.forEach(t -> {
            List<MatchState> next = branches.stream()
                    .flatMap(ms -> ms.nexts.entrySet().stream()
                            .map(e -> e.getKey().testInstance(t, matchContext) ? e.getValue() : null)
                            .filter(Objects::nonNull))
                    .collect(toList());

            branches.clear();
            branches.addAll(next);
        });

        return branches.stream().anyMatch(ms -> ms.terminal);
    }

    @Override
    protected <M> boolean testError(ErrorType t, MatchContext<M> matchContext) {
        return testDeclared(t, matchContext);
    }

    @Override
    protected <M> boolean testTypeVariable(TypeVariable t, MatchContext<M> matchContext) {
        return testInstance(t.getUpperBound(), matchContext);
    }

    private static final class MatchState {
        private final Map<TypeParameterMatch, MatchState> nexts = new IdentityHashMap<>(4);
        private boolean terminal;

        /**
         * This is sort of like an NFA for regexes, only our matches are not that complex. We only need to deal with
         * {@code **} which adds a little bit of complexity to the resolution. If it weren't for {@code **} we wouldn't
         * even need this class at all.
         *
         * @param matches the matches to create the match automaton from
         * @return a fully initialized match state usable for matching the list of actual type parameters on the tested
         * elements.
         */
        static MatchState createAutomaton(List<TypeParameterMatch> matches) {
            MatchState start = new MatchState();
            List<MatchState> currents = new ArrayList<>(2);
            currents.add(start);

            for (TypeParameterMatch m : matches) {
                if (m.isMatchAll()) {
                    // this matches 0 or more elements
                    MatchState next = new MatchState();
                    currents.forEach(ms -> ms.nexts.put(m, next));

                    // so that we can loop
                    next.nexts.put(m, next);

                    // don't clear the currents here so that we model the "0" matches
                    currents.add(next);
                } else {
                    // otherwise the simple case - we always proceed to the next match.
                    MatchState next = new MatchState();
                    currents.forEach(ms -> ms.nexts.put(m, next));

                    currents.clear();
                    currents.add(next);
                }
            }

            currents.forEach(ms -> ms.terminal = true);

            return start;
        }
    }
}
