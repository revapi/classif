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

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;

public final class SingleTypeReferenceMatch extends TypeInstanceMatch {
    private final FqnMatch fullyQualifiedName;
    private final TypeParametersMatch typeParameters;
    private final String variable;
    private final boolean negation;
    private final boolean isArray;

    private static final ElementVisitor<List<Element>, Void> TYPE_VARIABLE_TO_TYPE =
            new SimpleElementVisitor8<List<Element>, Void>(null) {
                @Override
                public List<Element> visitType(TypeElement e, Void __) {
                    return singletonList(e);
                }

                @Override
                public List<Element> visitTypeParameter(TypeParameterElement e, Void __) {
                    return e.getBounds().stream().flatMap(b -> TO_ELEMENT.visit(b, null).stream()).collect(toList());
                }

            };

    private static final TypeVisitor<List<Element>, Void> TO_ELEMENT = new SimpleTypeVisitor8<List<Element>, Void>(null) {
        @Override
        public List<Element> visitDeclared(DeclaredType t, Void __) {
            return singletonList(t.asElement());
        }

        @Override
        public List<Element> visitError(ErrorType t, Void __) {
            return visitDeclared(t, null);
        }

        @Override
        public List<Element> visitTypeVariable(TypeVariable t, Void __) {
            return TYPE_VARIABLE_TO_TYPE.visit(t.asElement());
        }
    };

    public SingleTypeReferenceMatch(FqnMatch fullyQualifiedName, TypeParametersMatch typeParameters,
            String variable, boolean negation, boolean isArray) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.typeParameters = typeParameters;
        this.variable = variable;
        this.negation = negation;
        this.isArray = isArray;
    }

    @Override
    protected <M> boolean testInstance(TypeMirror instance, MatchContext<M> ctx) {
        return instance.accept(new SimpleTypeVisitor8<Boolean, Void>(false) {
            @Override
            public Boolean visitPrimitive(PrimitiveType t, Void __) {
                return fullyQualifiedName != null && fullyQualifiedName.testInstance(instance, ctx);
            }

            @Override
            public Boolean visitArray(ArrayType t, Void __) {
                return isArray && t.getComponentType().accept(this, null);
            }

            @Override
            public Boolean visitDeclared(DeclaredType t, Void __) {
                return doTest(t, ctx);
            }

            @Override
            public Boolean visitError(ErrorType t, Void __) {
                return visitDeclared(t, null);
            }

            @Override
            public Boolean visitTypeVariable(TypeVariable t, Void __) {
                return visit(t.getUpperBound());
            }

            @Override
            public Boolean visitWildcard(WildcardType t, Void __) {
                if (t.getExtendsBound() != null) {
                    return visit(t.getExtendsBound());
                } else if (t.getSuperBound() != null) {
                    return visit(t.getSuperBound());
                } else {
                    TypeElement javaLangObject = ctx.modelInspector.getJavaLangObjectElement();
                    return doTest(javaLangObject.asType(), ctx);
                }
            }

            @Override
            public Boolean visitIntersection(IntersectionType t, Void aVoid) {
                return t.getBounds().stream().anyMatch(this::visit);
            }

            @Override
            public Boolean visitNoType(NoType t, Void __) {
                return fullyQualifiedName != null && fullyQualifiedName.testInstance(t, ctx);
            }
        }, null);
    }

    private <M> boolean doTest(TypeMirror instance, MatchContext<M> ctx) {
        boolean ret;
        if (fullyQualifiedName != null) {
            ret = fullyQualifiedName.testInstance(instance, ctx);
            if (typeParameters != null) {
                ret = typeParameters.testInstance(instance, ctx);
            }
        } else {
            ModelMatch match = ctx.variables.getOrDefault(variable, null);
            ret = match != null
                    && TO_ELEMENT.visit(instance).stream()
                    .anyMatch(e -> match.test(ctx.modelInspector.fromElement(e), ctx));
        }

        return negation != ret;
    }
}
