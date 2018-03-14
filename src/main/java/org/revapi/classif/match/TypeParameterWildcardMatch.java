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
package org.revapi.classif.match;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor8;

public final class TypeParameterWildcardMatch extends TypeInstanceMatch {
    private final int index;
    private final boolean isExtends;
    private final List<TypeReferenceMatch> bounds;

    private final SimpleTypeVisitor8<Boolean, MatchContext<?>> visitor;

    public TypeParameterWildcardMatch(int index, boolean isExtends, List<TypeReferenceMatch> bounds) {
        this.index = index;
        this.isExtends = isExtends;
        this.bounds = bounds;

        visitor = new SimpleTypeVisitor8<Boolean, MatchContext<?>>(false) {

        };
    }

    @Override
    protected <M> boolean defaultTest(TypeMirror instantiation, MatchContext<M> ctx) {
        if (!(instantiation instanceof DeclaredType)) {
            return false;
        }

        DeclaredType type = (DeclaredType) instantiation;

        if (type.getTypeArguments().size() <= index) {
            return false;
        }

        TypeMirror typeArg = type.getTypeArguments().get(index);

        return typeArg.accept(visitor, ctx);
    }
}
