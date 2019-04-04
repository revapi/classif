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

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
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
import org.revapi.classif.util.Nullable;

final class UseVisitor {

    private UseVisitor() {

    }

    /**
     * Returns a visitor to find out the stream of directly used types of some element.
     *
     * <p>Note that the visitor keeps track of the types it visited and will return an empty stream for types it already
     * has seen.
     *
     * @param insp        the inspector to find uses of types
     * @param <M>         the type of the model elements
     * @return a visitor returning a stream of direct uses of some element.
     */
    static <M> TypeVisitor<@Nullable Stream<DeclaredType>, ?> findUses(ModelInspector<M> insp) {
        return new SimpleTypeVisitor8<Stream<DeclaredType>, Void>(empty()) {
            private HashSet<TypeMirror> visited = new HashSet<>();
            private int depth;

            @Override
            public Stream<DeclaredType> visitIntersection(IntersectionType t, Void __) {
                return nest(() -> t.getBounds().stream().flatMap(b -> nest(() -> visit(b, null))));
            }

            @Override
            public Stream<DeclaredType> visitArray(ArrayType t, Void __) {
                return nest(() -> visit(t.getComponentType(), null));
            }

            @Override
            public Stream<DeclaredType> visitDeclared(DeclaredType t, Void __) {
                if (visited.contains(t)) {
                    return empty();
                }

                visited.add(t);

                // the depth of the visit tells us whether we are being asked to directly find uses of a type
                // (depth == 0) or if we are trying to find uses of something else which led us to this method
                // (depth != 0)
                // if we're finding uses of the type itself, we don't want it to end up in the results, which is exactly
                // what we want to do if we're not finding uses of the type itself.

                Stream<DeclaredType> ret = depth == 0 ? Stream.empty() : Stream.of(t);

                try {
                    depth++;
                    if (depth == 1) {
                        for (TypeMirror typeMirror : t.getTypeArguments()) {
                            ret = concat(ret, visit(typeMirror, null));
                        }

                        for (TypeMirror st : insp.directSupertypes(t)) {
                            ret = concat(ret, visit(st, null));
                        }

                        // we're looking for uses of the type, so just append anything the inspector wants us to consider
                        // a use on top of what we already know ourselves.
                        TypeElement type = (TypeElement) t.asElement();
                        Stream<DeclaredType> modelledUses = modelledUses(type);

                        return modelledUses == null ? null : concat(ret, modelledUses);
                    } else {
                        return ret;
                    }
                } finally {
                    depth--;
                }
            }

            @Override
            public Stream<DeclaredType> visitError(ErrorType t, Void __) {
                //not setting the firstVisit to false here intentionally. This method is just a proxy to visitDeclared()
                return visitDeclared(t, null);
            }

            @Override
            public Stream<DeclaredType> visitTypeVariable(TypeVariable t, Void __) {
                return nest(() -> visit(t.getUpperBound(), null));
            }

            @Override
            public Stream<DeclaredType> visitWildcard(WildcardType t, Void __) {
                return nest(() -> {
                    if (t.getSuperBound() != null) {
                        return visit(t.getSuperBound(), null);
                    } else if (t.getExtendsBound() != null) {
                        return visit(t.getExtendsBound(), null);
                    } else {
                        return visit(insp.getJavaLangObjectElement().asType(), null);
                    }
                });
            }

            @Override
            public Stream<DeclaredType> visitExecutable(ExecutableType t, Void __) {
                return nest(() -> {
                    Stream<DeclaredType> retType = visit(t.getReturnType(), null);

                    Stream<DeclaredType> paramTypes = t.getParameterTypes().stream()
                            .flatMap(p -> nest(() -> visit(p, null)));

                    Stream<DeclaredType> thrownTypes = t.getThrownTypes().stream()
                            .flatMap(e -> nest(() -> visit(e, null)));

                    Stream<DeclaredType> typeParamTypes = t.getTypeVariables().stream()
                            .flatMap(v -> nest(() -> visit(v, null)));

                    return concat(concat(concat(retType, paramTypes), thrownTypes), typeParamTypes);
                });
            }

            private Stream<DeclaredType> modelledUses(TypeElement t) {
                return insp.getUses(insp.fromElement(t)).stream().map(type -> (DeclaredType) insp.toMirror(type));
            }

            private <T> T nest(Supplier<T> fn) {
                try {
                    depth++;
                    return fn.get();
                } finally {
                    depth--;
                }
            }
        };
    }

    /**
     * Returns a visitor to find out the stream of elements directly using the provided types.
     *
     * <p>Note that the visitor keeps track of the types it visited and will return an empty stream for types it already
     * has seen.
     *
     * @param insp        the inspector to find uses of types
     * @param <M>         the type of the model elements
     * @return a visitor returning a stream of direct use sites of some type
     */
    static <M> TypeVisitor<@Nullable Stream<Element>, ?> findUseSites(ModelInspector<M> insp) {
        return new SimpleTypeVisitor8<@Nullable Stream<Element>, Object>(Stream.empty()) {
            private HashSet<DeclaredType> visited = new HashSet<>();

            @Override
            public @Nullable Stream<Element> visitDeclared(DeclaredType t, Object o) {
                if (visited.contains(t)) {
                    return empty();
                }

                visited.add(t);

                Element el = t.asElement();
                M model = insp.fromElement(el);

                Set<M> useSites = insp.getUseSites(model);
                return useSites == null ? null : useSites.stream().map(insp::toElement);
            }
        };
    }
}
