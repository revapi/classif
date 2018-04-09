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

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

import java.util.HashSet;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor8;

import org.revapi.classif.ModelInspector;
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
    public <M> boolean testDeclaration(Element declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        TypeVisitor<Stream<DeclaredType>, Void> visitor = getVisitor(ctx.modelInspector, !onlyDirect);

        Stream<DeclaredType> directUses = visitor.visit(instantiation);

        if (onlyDirect) {
            return directUses.anyMatch(u -> type.testInstance(u, ctx));
        } else {
            return testRecursively(directUses, visitor, ctx);
        }
    }

    private boolean testRecursively(Stream<DeclaredType> types, TypeVisitor<Stream<DeclaredType>, Void> visitor,
            MatchContext<?> ctx) {

        return types.reduce(
                false,
                (prior, next) -> prior
                        || type.testInstance(next, ctx)
                        || testRecursively(visitor.visit(next), visitor, ctx),
                Boolean::logicalOr);
    }

    private static <M> TypeVisitor<Stream<DeclaredType>, Void> getVisitor(ModelInspector<M> insp,
            boolean includeModelledUses) {

        return new SimpleTypeVisitor8<Stream<DeclaredType>, Void>(empty()) {
            private HashSet<TypeMirror> visited = new HashSet<>();

            @Override
            public Stream<DeclaredType> visitIntersection(IntersectionType t, Void __) {
                return t.getBounds().stream().flatMap(b -> visit(b, null));
            }

            @Override
            public Stream<DeclaredType> visitArray(ArrayType t, Void __) {
                return visit(t.getComponentType(), null);
            }

            @Override
            public Stream<DeclaredType> visitDeclared(DeclaredType t, Void __) {
                if (visited.contains(t)) {
                    return empty();
                }

                visited.add(t);

                Stream<DeclaredType> ret = Stream.of(t);
                for (TypeMirror typeMirror : t.getTypeArguments()) {
                    ret = concat(ret, visit(typeMirror, null));
                }

                if (includeModelledUses) {
                    TypeElement type = (TypeElement) t.asElement();

                    ret = concat(ret, modelledUses(type, insp));
                }

                return ret;
            }

            @Override
            public Stream<DeclaredType> visitError(ErrorType t, Void __) {
                return visitDeclared(t, null);
            }

            @Override
            public Stream<DeclaredType> visitTypeVariable(TypeVariable t, Void __) {
                return visit(t.getUpperBound(), null);
            }

            @Override
            public Stream<DeclaredType> visitWildcard(WildcardType t, Void __) {
                if (t.getSuperBound() != null) {
                    return visit(t.getSuperBound(), null);
                } else if (t.getExtendsBound() != null) {
                    return visit(t.getExtendsBound(), null);
                } else {
                    return visit(insp.getJavaLangObjectElement().asType(), null);
                }
            }

            @Override
            public Stream<DeclaredType> visitExecutable(ExecutableType t, Void __) {
                Stream<DeclaredType> retType = visit(t.getReturnType(), null);
                Stream<DeclaredType> paramTypes = t.getParameterTypes().stream()
                        .flatMap(p -> visit(p, null));
                Stream<DeclaredType> thrownTypes = t.getThrownTypes().stream()
                        .flatMap(e -> visit(e, null));
                Stream<DeclaredType> typeParamTypes = t.getTypeVariables().stream()
                        .flatMap(v -> visit(v, null));

                return concat(concat(concat(retType, paramTypes), thrownTypes), typeParamTypes);
            }

            private Stream<DeclaredType> modelledUses(TypeElement t, ModelInspector<M> insp) {
                return insp.getUses(insp.fromType(t)).stream()
                        .map(type -> (DeclaredType) insp.toMirror(type));
            }
        };
    }
}
