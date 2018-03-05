/*
 * Copyright 2014-2018 Lukas Krejci
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.revapi.classif.statement.AbstractMatcher;
import org.revapi.classif.statement.AbstractStatement;

/**
 * The main entry point for matching the elements against a recipe. An instance of this class can be obtained from
 * {@link Classif#compile(String)}.
 *
 * @see #test(Object, ModelInspector)
 */
public final class StructuralMatcher {
    private final List<AbstractMatcher> matchers;
    private final Map<String, AbstractMatcher> variables;
    private final boolean decidableInPlace;

    StructuralMatcher(List<String> namedMatches, List<AbstractStatement> statements) {
        this.matchers = new ArrayList<>();
        this.variables = new HashMap<>();

        this.decidableInPlace = initMatchEvaluators(null, namedMatches, statements);
    }

    private boolean initMatchEvaluators(AbstractMatcher parentMatcher, List<String> namedMatches,
            Collection<AbstractStatement> statements) {

        boolean decidableInPlace = true;

        for (AbstractStatement st : statements) {
            AbstractMatcher stMatcher = st.createMatcher();
            stMatcher.setParent(parentMatcher);

            if (st.isMatch() || namedMatches.contains(st.getDefinedVariable())) {
                this.matchers.add(stMatcher);
            }

            if (st.getDefinedVariable() != null) {
                this.variables.put(st.getDefinedVariable(), stMatcher);
            }

            decidableInPlace = decidableInPlace && st.getReferencedVariables().isEmpty();

            decidableInPlace = initMatchEvaluators(stMatcher, namedMatches, st.getChildren()) && decidableInPlace;
        }

        return decidableInPlace;
    }

    /**
     * Tells whether this program can decide about some element solely based on the element itself or whether it needs
     * to crawl around the element graph to establish the match.
     */
    public boolean isDecidableInPlace() {
        return decidableInPlace;
    }

    /**
     * Tests whether this match program matches given model element.
     *
     * @param model the model representing some Java construct
     * @param inspector the inspector of the model elements
     * @param <E> the type of the model
     * @return a match describing the result of the test
     */
    public <E> boolean test(E model, ModelInspector<E> inspector) {
        return matchers.stream().reduce(false,
                (prior, unevaluated) -> prior || unevaluated.test(model, inspector, variables), (a, b) -> a || b);
    }
}
