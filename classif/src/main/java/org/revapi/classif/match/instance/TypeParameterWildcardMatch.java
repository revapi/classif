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

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;

public final class TypeParameterWildcardMatch extends TypeInstanceMatch {
    private final boolean isExtends;
    private final List<TypeReferenceMatch> bounds;

    public TypeParameterWildcardMatch(boolean isExtends, List<TypeReferenceMatch> bounds) {
        this.isExtends = isExtends;
        this.bounds = bounds;
    }

    @Override
    protected <M> TestResult testWildcard(WildcardType t, MatchContext<M> matchContext) {
        TypeMirror bound = isExtends ? t.getExtendsBound() : t.getSuperBound();
        if (bound == null) {
            bound = matchContext.getModelInspector().getJavaLangObjectElement().asType();
        }

        final TypeMirror b = bound;
        return testable(bounds).testAll(m -> m.testInstance(b, matchContext));
    }

    @Override
    public String toString() {
        return "?" + (isExtends ? " extends " : " super ")
                + bounds.stream().map(Object::toString).collect(Collectors.joining(" & "));
    }
}
