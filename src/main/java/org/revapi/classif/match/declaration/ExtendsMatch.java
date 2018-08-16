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

import static javax.lang.model.type.TypeKind.NONE;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor8;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.instance.TypeReferenceMatch;

public final class ExtendsMatch extends DeclarationMatch {
    private static final TypeVisitor<TypeMirror, Void> GET_SUPER_CLASS = new SimpleTypeVisitor8<TypeMirror, Void>() {
        @Override
        public TypeMirror visitDeclared(DeclaredType t, Void __) {
            return ((TypeElement) t.asElement()).getSuperclass();
        }

        @Override
        public TypeMirror visitError(ErrorType t, Void __) {
            return visitDeclared(t, null);
        }
    };

    private final boolean onlyDirect;
    private final TypeReferenceMatch superTypeMatch;

    public ExtendsMatch(boolean onlyDirect, TypeReferenceMatch superTypeMatch) {
        this.onlyDirect = onlyDirect;
        this.superTypeMatch = superTypeMatch;
    }

    @Override
    protected <M> TestResult testType(TypeElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        if (onlyDirect) {
            return superTypeMatch.testInstance(declaration.getSuperclass(), ctx);
        } else {
            return someSuperTypeMatches(declaration.getSuperclass(), ctx);
        }
    }

    private <M> TestResult someSuperTypeMatches(TypeMirror superType, MatchContext<M> ctx) {
        TestResult ret = TestResult.NOT_PASSED;
        while (superType != null && superType.getKind() != NONE) {
            ret = ret.or(superTypeMatch.testInstance(superType, ctx));

            if (ret.toBoolean(false)) {
                return ret;
            }

            superType = GET_SUPER_CLASS.visit(superType);
        }

        return ret;
    }

// this complexity most probably not needed because this match matches the declaration, not a type instance.
//    private TypeMirror superTypeOf(TypeMirror type, MatchContext<?> ctx) {
//        return type.accept(new SimpleTypeVisitor8<TypeMirror, Void>() {
//            @Override
//            public TypeMirror visitDeclared(DeclaredType t, Void __) {
//                List<? extends TypeMirror> allTypeArgs = t.getTypeArguments();
//                List<? extends TypeParameterElement> typeParams = ((TypeElement) t.asElement()).getTypeParameters();
//
//                TypeMirror superType = ((TypeElement) t.asElement()).getSuperclass();
//                if (superType.getKind() == DECLARED || superType.getKind() == ERROR) {
//                    List<? extends TypeMirror> superTypeArgs = ((DeclaredType) superType).getTypeArguments();
//
//                    if (superTypeArgs.isEmpty()) {
//                        return superType;
//                    }
//
//                    // we're trying to map the actual type parameters in the allTypeArgs onto the type parameters found
//                    // on the superType. This is because superType is obtained from the element instead of a type mirror
//                    // and therefore we might be losing some type information.
//
//                    superTypeArgs = new ArrayList<>(superTypeArgs);
//
//                    @SuppressWarnings("unchecked")
//                    ListIterator<TypeMirror> it = ((List<TypeMirror>)superTypeArgs).listIterator();
//                    boolean changed = false;
//                    while (it.hasNext()) {
//                        TypeMirror sta = it.next();
//                        if (sta.getKind() == TYPEVAR) {
//                            Element staEl = ((TypeVariable) sta).asElement();
//                            @SuppressWarnings("SuspiciousMethodCalls")
//                            int staIdx = typeParams.indexOf(staEl);
//                            TypeMirror actual = allTypeArgs.get(staIdx);
//                            it.set(actual);
//                            changed = true;
//                        }
//                    }
//
//                    if (changed) {
//                        TypeElement el = (TypeElement) ((DeclaredType) superType).asElement();
//                        superType = ctx.modelInspector.declaredType(null, el, superTypeArgs.toArray(new TypeMirror[superTypeArgs.size()]));
//                    }
//
//                    return superType;
//                } else {
//                    return superType;
//                }
//            }
//        }, null);
//    }
}
