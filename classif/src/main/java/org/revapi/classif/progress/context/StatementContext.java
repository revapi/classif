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

import java.util.Map;
import java.util.Set;

import org.revapi.classif.ModelInspector;
import org.revapi.classif.TestResult;
import org.revapi.classif.util.Nullable;

public final class StatementContext<M> {
    private final MatchContext<M> matchContext;
    private final @Nullable String definedVariable;
    private final boolean isReturn;

    @Override
    public String toString() {
        return "StatementContext{" +
                "matchContext=" + matchContext +
                ", definedVariable='" + definedVariable + '\'' +
                ", isReturn=" + isReturn +
                '}';
    }

    public StatementContext(ModelInspector<M> modelInspector, boolean isReturn, @Nullable String definedVariable,
            Set<String> variables) {
        this(new MatchContext<>(modelInspector, variables), definedVariable, isReturn);
    }

    private StatementContext(MatchContext<M> ctx, @Nullable String definedVariable, boolean isReturn) {
        this.definedVariable = definedVariable;
        this.matchContext = ctx;
        this.isReturn = isReturn;
    }

    public StatementContext<M> require(Map<String, M> mapping) {
        return new StatementContext<>(matchContext.require(mapping), definedVariable, isReturn);
    }

    public StatementContext<M> withResults(Map<String, TestResult> mapping) {
        return new StatementContext<>(matchContext.withResults(mapping), definedVariable, isReturn);
    }

    public boolean isReturn() {
        return isReturn;
    }

    public String getDefinedVariable() {
        return definedVariable;
    }

    public MatchContext<M> getMatchContext() {
        return matchContext;
    }

    public ModelInspector<M> getModelInspector() {
        return matchContext.getModelInspector();
    }

    public Set<String> getVariables() {
        return matchContext.getVariables();
    }
}
