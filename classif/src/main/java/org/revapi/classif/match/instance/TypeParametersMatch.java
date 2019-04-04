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
package org.revapi.classif.match.instance;

import static org.revapi.classif.TestResult.TestableStream.testable;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeVariable;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.util.Glob;

public final class TypeParametersMatch extends TypeInstanceMatch {
    private final Glob<TypeParameterMatch> glob;

    public TypeParametersMatch(List<TypeParameterMatch> matches) {
        this.glob = new Glob<>(matches);
    }

    @Override
    protected <M> TestResult testIntersection(IntersectionType t, MatchContext<M> matchContext) {
        return testable(t.getBounds()).testAny(b -> testInstance(b, matchContext));
    }

    @Override
    protected <M> TestResult testDeclared(DeclaredType type, MatchContext<M> matchContext) {
        return glob.test((m, t) -> m.testInstance(t, matchContext), type.getTypeArguments());
    }

    @Override
    protected <M> TestResult testExecutable(ExecutableType method, MatchContext<M> matchContext) {
        return glob.test((m, t) -> m.testInstance(t, matchContext), method.getTypeVariables());
    }

    @Override
    protected <M> TestResult testError(ErrorType t, MatchContext<M> matchContext) {
        return testDeclared(t, matchContext);
    }

    @Override
    protected <M> TestResult testTypeVariable(TypeVariable t, MatchContext<M> matchContext) {
        return testInstance(t.getUpperBound(), matchContext);
    }

    @Override
    public String toString() {
        return glob.getMatches().stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}
