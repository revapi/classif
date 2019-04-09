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

import static org.revapi.classif.TestResult.TestableStream.testable;

import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.instance.TypeReferenceMatch;

public final class UsesMatch extends DeclarationMatch {

    private final boolean onlyDirect;
    private final TypeReferenceMatch type;

    public UsesMatch(boolean onlyDirect, TypeReferenceMatch type) {
        this.onlyDirect = onlyDirect;
        this.type = type;
    }

    @Override
    public <M> TestResult testAnyDeclaration(Element declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        TypeVisitor<Stream<DeclaredType>, ?> visitor = UseVisitor.findUses(ctx.modelInspector);

        Stream<DeclaredType> directUses = visitor.visit(instantiation);

        if (directUses == null) {
            return TestResult.DEFERRED;
        }

        if (onlyDirect) {
            return testable(directUses).testAny(u -> type.testInstance(u, ctx));
        } else {
            return testRecursively(directUses, ctx, UseVisitor.findUses(ctx.modelInspector));
        }
    }

    @Override
    public String toString() {
        return (onlyDirect ? "directly " : "") + "uses " + type;
    }

    private TestResult testRecursively(Stream<DeclaredType> types,
            MatchContext<?> ctx, TypeVisitor<Stream<DeclaredType>, ?> visitor) {

        return types == null ? TestResult.DEFERRED : testable(types).testAny(t -> type.testInstance(t, ctx).or(() ->
                testRecursively(visitor.visit(t), ctx, visitor)));
    }

}
