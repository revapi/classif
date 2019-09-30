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
package org.revapi.classif.progress.context;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.revapi.classif.ModelInspector;
import org.revapi.classif.TestResult;
import org.revapi.classif.progress.StatementMatch;
import org.revapi.classif.util.Nullable;

public final class MatchContext<M> {

    private final ModelInspector<M> modelInspector;
    private final Map<String, StatementMatch<M>> referencedStatements;

    public MatchContext(ModelInspector<M> modelInspector, Set<String> variables) {
        this(requireNonNull(modelInspector),
                requireNonNull(variables).stream().collect(toMap(identity(), __ -> AlwaysMatch.instance())));
    }

    private MatchContext(ModelInspector<M> modelInspector, Map<String, StatementMatch<M>> referencedStatements) {
        this.modelInspector = modelInspector;
        this.referencedStatements = referencedStatements;
    }

    // null model means that that value is not determined and therefore never passes any test
    public MatchContext<M> require(Map<String, @Nullable M> mapping) {
        Map<String, StatementMatch<M>> newVars = new HashMap<>(referencedStatements);
        mapping.forEach((var, requiredModel) -> newVars.put(var, new ExactMatch<>(requiredModel)));

        return new MatchContext<>(modelInspector, newVars);
    }

    public MatchContext<M> withResults(Map<String, TestResult> mapping) {
        Map<String, StatementMatch<M>> newVars = new HashMap<>(referencedStatements);
        mapping.forEach((var, result) -> newVars.put(var, new DefinedMatch<>(result)));
        return new MatchContext<>(modelInspector, newVars);
    }

    public ModelInspector<M> getModelInspector() {
        return modelInspector;
    }

    public StatementMatch<M> getVariableMatcher(String variable) {
        return referencedStatements.getOrDefault(variable, AlwaysMatch.instance());
    }

    public Set<String> getVariables() {
        return referencedStatements.keySet();
    }

    @Override
    public String toString() {
        return "MatchContext{" +
                "referencedStatements=" + referencedStatements +
                '}';
    }

    private static final class ExactMatch<M> extends StatementMatch<M> {
        private final @Nullable M model;

        private ExactMatch(@Nullable M model) {
            this.model = model;
        }

        @Override
        protected TestResult defaultElementTest(M model, MatchContext<M> ctx) {
            return TestResult.fromBoolean(Objects.equals(model, this.model));
        }

        @Override
        public String toString() {
            return "ExactMatch{" +
                    "model=" + model +
                    '}';
        }
    }

    private static final class AlwaysMatch<M> extends StatementMatch<M> {
        @SuppressWarnings("rawtypes")
        static final AlwaysMatch<?> INSTANCE = new AlwaysMatch();

        @SuppressWarnings("unchecked")
        static <M> AlwaysMatch<M> instance() {
            return (AlwaysMatch<M>) INSTANCE;
        }

        @Override
        protected TestResult defaultElementTest(M model, MatchContext<M> ctx) {
            return TestResult.PASSED;
        }

        @Override
        public String toString() {
            return "AlwaysMatch";
        }
    }

    private static final class DefinedMatch<M> extends StatementMatch<M> {
        private final TestResult result;

        DefinedMatch(TestResult result) {
            this.result = result;
        }

        @Override
        protected TestResult defaultElementTest(M model, MatchContext<M> ctx) {
            return result;
        }
    }
}
