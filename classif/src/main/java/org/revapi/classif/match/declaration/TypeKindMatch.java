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

import static java.util.Objects.requireNonNull;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;

public final class TypeKindMatch extends DeclarationMatch {
    private final boolean negation;
    private final TypeKind kind;

    public TypeKindMatch(boolean negation, TypeKind typeKind) {
        this.negation = negation;
        this.kind = requireNonNull(typeKind);
    }

    @Override
    protected <M> TestResult testType(TypeElement type, TypeMirror instantiation, MatchContext<M> ctx) {
        boolean matches = kind.matches(type.getKind());

        return TestResult.fromBoolean(negation != matches);
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        if (negation) {
            bld.append("!");
        }

        bld.append(kind.toString());

        return bld.toString();
    }
}
