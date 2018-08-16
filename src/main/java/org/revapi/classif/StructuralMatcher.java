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

import java.util.List;

import org.revapi.classif.statement.AbstractStatement;
import org.revapi.classif.util.execution.DependencyGraph;

/**
 * The main entry point for matching the elements against a recipe. An instance of this class can be obtained from
 * {@link Classif#compile(String)}.
 *
 * @see #with(ModelInspector)
 */
public final class StructuralMatcher {
    private final DependencyGraph matchTree;

    StructuralMatcher(List<String> namedMatches, List<AbstractStatement> statements) {
        this.matchTree = new DependencyGraph(namedMatches, statements);
    }

    /**
     * Starts a matching progress using the provided model inspector.
     *
     * @param inspector the model inspector used when matching the model elements
     * @param <M> the type of the model elements
     * @return a matching progress
     */
    public <M> MatchingProgress<M> with(ModelInspector<M> inspector) {
        return new MatchingProgress<>(matchTree, inspector);
    }
}
