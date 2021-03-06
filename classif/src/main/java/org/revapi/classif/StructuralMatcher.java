/*
 * Copyright 2018-2019 Lukas Krejci
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

import org.revapi.classif.progress.MatchingProgress;
import org.revapi.classif.statement.AbstractStatement;
import org.revapi.classif.util.execution.DependencyGraph;

/**
 * The main entry point for matching the elements against a recipe. An instance of this class can be obtained from
 * {@link Classif#match()} builder.
 *
 * @see #with(ModelInspector)
 */
public final class StructuralMatcher {
    private final DependencyGraph matchTree;
    private final Configuration configuration;

    public StructuralMatcher(Configuration configuration, List<String> namedMatches, List<AbstractStatement> statements) {
        this.matchTree = new DependencyGraph(namedMatches, statements);
        this.configuration = configuration;
    }

    /**
     * Starts a matching progress using the provided model inspector.
     *
     * @param inspector the model inspector used when matching the model elements
     * @param <M> the type of the model elements
     * @return a matching progress
     */
    public <M> MatchingProgress<M> with(ModelInspector<M> inspector) {
        return MatchingProgress.of(matchTree, inspector, configuration);
    }

    @Override
    public String toString() {
        return configuration.toString() + "\n" + matchTree.toString();
    }

    public static final class Configuration {
        private final boolean strictHierarchy;

        public Configuration(boolean strictHierarchy) {
            this.strictHierarchy = strictHierarchy;
        }

        public boolean isStrictHierarchy() {
            return strictHierarchy;
        }

        @Override
        public String toString() {
            if (strictHierarchy) {
                return "#strictHierarchy;";
            } else {
                return "";
            }
        }
    }
}
