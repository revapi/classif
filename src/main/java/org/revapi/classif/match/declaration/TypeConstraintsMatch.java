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

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.match.MatchContext;

public final class TypeConstraintsMatch extends DeclarationMatch {
    private final List<ImplementsMatch> implemented;
    private final List<UsesMatch> uses;

    public TypeConstraintsMatch(List<ImplementsMatch> implemented, List<UsesMatch> uses) {
        this.implemented = implemented;
        this.uses = uses;
    }

    public boolean isDecidableInPlace() {
        return uses == null;
    }

    @Override
    public <M> boolean testDeclaration(Element declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        // TODO add the rest of the type constraints
        return implemented.stream().allMatch(m -> m.testDeclaration(declaration, instantiation, ctx))
                && uses.stream().allMatch(m -> m.testDeclaration(declaration, instantiation, ctx));
    }
}
