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
package org.revapi.classif.match.declaration;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.util.Glob;

public final class ThrowsMatch extends DeclarationMatch {
    private final Glob<TypeReferenceMatch> thrownTypes;

    public ThrowsMatch(List<TypeReferenceMatch> thrownTypes) {
        this.thrownTypes = new Glob<>(thrownTypes);
    }

    @Override
    protected <M> TestResult testMethod(ExecutableElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        if (instantiation.getKind() != TypeKind.EXECUTABLE) {
            return TestResult.NOT_PASSED;
        }

        ExecutableType method = (ExecutableType) instantiation;

        List<? extends TypeMirror> thrown = method.getThrownTypes();

        return thrownTypes.testUnordered((m, t) -> m.testInstance(t, ctx), thrown);
    }

    @Override
    public String toString() {
        return "throws " + thrownTypes.getMatches().stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}
