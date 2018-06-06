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
 * @see #start(ModelInspector)
 */
public final class StructuralMatcher {
    private final DependencyGraph matchTree;

    StructuralMatcher(List<String> namedMatches, List<AbstractStatement> statements) {
        this.matchTree = new DependencyGraph(namedMatches, statements);
    }

    /**
     * Tells whether this program can decide about some element solely based on the element itself or whether it needs
     * to crawl around the element graph to establish the match.
     */
    public boolean isDecidableInPlace() {
        //TODO is this even needed?
        return false;
    }

    public <M> MatchingProgress<M> start(ModelInspector<M> inspector) {
        return new MatchingProgress<>(matchTree, inspector);
    }
}
