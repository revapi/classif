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
package org.revapi.classif.match.declaration;

import static org.revapi.classif.TestResult.PASSED;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.util.Globbed;
import org.revapi.classif.util.Nullable;

public final class MethodParameterMatch extends DeclarationMatch implements Globbed {
    private final @Nullable AnnotationsMatch annotations;
    private final TypeReferenceMatch type;

    public MethodParameterMatch(@Nullable AnnotationsMatch annotations, TypeReferenceMatch type) {
        this.annotations = annotations;
        this.type = type;
    }

    @Override
    protected <M> TestResult testFieldOrArgument(VariableElement declaration, TypeMirror instantiation,
            MatchContext<M> ctx) {
        return (annotations == null ? PASSED : annotations.defaultTest(declaration, instantiation, ctx))
                .and(type.testInstance(instantiation, ctx));
    }

    @Override
    public boolean isMatchAny() {
        return type.isMatchAny();
    }

    @Override
    public boolean isMatchAll() {
        return type.isMatchAll();
    }
}
