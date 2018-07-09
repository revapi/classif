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
import static org.revapi.classif.TestResult.TestableStream.testable;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;

public final class TypeConstraintsMatch extends DeclarationMatch {
    private final List<ImplementsMatch> implemented;
    private final ExtendsMatch extended;
    private final List<UsesMatch> uses;
    private final List<UsedByMatch> usedBys;

    public TypeConstraintsMatch(List<ImplementsMatch> implemented, ExtendsMatch extended, List<UsesMatch> uses,
            List<UsedByMatch> usedBys) {
        this.implemented = implemented;
        this.extended = extended;
        this.uses = uses;
        this.usedBys = usedBys;
    }

    @Override
    public <M> TestResult testDeclaration(Element declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        return testable(implemented).testAll(m -> m.testDeclaration(declaration, instantiation, ctx))
                .and(() -> testable(uses).testAll(m -> m.testDeclaration(declaration, instantiation, ctx)))
                .and(() -> testable(usedBys).testAll(m -> m.testDeclaration(declaration, instantiation, ctx)))
                .and(() -> extended == null ? PASSED : extended.testDeclaration(declaration, instantiation, ctx));
    }
}
