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
package org.revapi.classif.match.instance;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor8;

import org.revapi.classif.match.MatchContext;

public final class TypeParameterWildcardMatch extends TypeInstanceMatch {
    private final boolean isExtends;
    private final List<TypeReferenceMatch> bounds;

    public TypeParameterWildcardMatch(boolean isExtends, List<TypeReferenceMatch> bounds) {
        this.isExtends = isExtends;
        this.bounds = bounds;
    }

    @Override
    protected <M> boolean testWildcard(WildcardType t, MatchContext<M> matchContext) {
        TypeMirror bound = isExtends ? t.getExtendsBound() : t.getSuperBound();
        if (bound == null) {
            bound = matchContext.modelInspector.getJavaLangObjectElement().asType();
        }

        final TypeMirror b = bound;
        return bounds.stream().allMatch(m -> m.testInstance(b, matchContext));
    }
}
