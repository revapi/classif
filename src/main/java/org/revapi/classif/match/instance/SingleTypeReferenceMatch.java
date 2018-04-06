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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.util.List;

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

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.match.util.Globbed;

public final class SingleTypeReferenceMatch extends TypeInstanceMatch implements Globbed {
    private final FqnMatch fullyQualifiedName;
    private final TypeParametersMatch typeParameters;
    private final String variable;
    private final boolean negation;
    private final int arrayDimension;

    private static final ElementVisitor<List<TypeElement>, Void> TYPE_VARIABLE_TO_TYPE =
            new SimpleElementVisitor8<List<TypeElement>, Void>(emptyList()) {
                @Override
                public List<TypeElement> visitType(TypeElement e, Void __) {
                    return singletonList(e);
                }

                @Override
                public List<TypeElement> visitTypeParameter(TypeParameterElement e, Void __) {
                    return e.getBounds().stream().flatMap(b -> TO_TYPE.visit(b, null).stream()).collect(toList());
                }

            };

    private static final TypeVisitor<List<TypeElement>, Void> TO_TYPE = new SimpleTypeVisitor8<List<TypeElement>, Void>(emptyList()) {
        @Override
        public List<TypeElement> visitDeclared(DeclaredType t, Void __) {
            return singletonList((TypeElement) t.asElement());
        }

        @Override
        public List<TypeElement> visitError(ErrorType t, Void __) {
            return visitDeclared(t, null);
        }

        @Override
        public List<TypeElement> visitTypeVariable(TypeVariable t, Void __) {
            return TYPE_VARIABLE_TO_TYPE.visit(t.asElement());
        }
    };

    public SingleTypeReferenceMatch(FqnMatch fullyQualifiedName, TypeParametersMatch typeParameters,
            String variable, boolean negation, int arrayDimension) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.typeParameters = typeParameters;
        this.variable = variable;
        this.negation = negation;
        this.arrayDimension = arrayDimension;
    }

    @Override
    public boolean isMatchAny() {
        return fullyQualifiedName != null && fullyQualifiedName.isMatchAny() && arrayDimension == 0 && !negation
                && typeParameters == null;
    }

    @Override
    public boolean isMatchAll() {
        return fullyQualifiedName != null && fullyQualifiedName.isMatchAll() && arrayDimension == 0 && !negation
                && typeParameters == null;
    }

    @Override
    public <M> boolean testInstance(TypeMirror instance, MatchContext<M> ctx) {
        return instance.accept(new SimpleTypeVisitor8<Boolean, Void>(false) {
            @Override
            public Boolean visitPrimitive(PrimitiveType t, Void __) {
                return fullyQualifiedName != null && fullyQualifiedName.testInstance(instance, ctx);
            }

            @Override
            public Boolean visitArray(ArrayType t, Void __) {
                int dim = arrayDimension;

                TypeMirror m = t;
                while (m instanceof ArrayType) {
                    dim--;
                    m = ((ArrayType) m).getComponentType();
                }

                return dim == 0 && m.accept(this, null);
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
                    && TO_TYPE.visit(instance).stream()
                    .anyMatch(e -> match.test(ctx.modelInspector.fromType(e), ctx));
        }

        return negation != ret;
    }
}