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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.revapi.classif.ModelInspector;
import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.util.Nullable;

public final class OverridesMatch extends DeclarationMatch {

    private final @Nullable TypeReferenceMatch declaringType;

    public OverridesMatch(@Nullable TypeReferenceMatch declaringType) {
        this.declaringType = declaringType;
    }

    @Override
    protected <M> TestResult testMethod(ExecutableElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        if (instantiation.getKind() != TypeKind.EXECUTABLE) {
            return TestResult.NOT_PASSED;
        }

        TypeElement methodDeclaringType = (TypeElement) declaration.getEnclosingElement();

        // loop through the super types and if any of them matches
        // the declaringType, check if that type contains a method that is overridden by the provided method.
        ModelInspector<M> insp = ctx.modelInspector;
        for (TypeMirror type : allSuperTypes(methodDeclaringType.asType(), insp)) {
            if (declaringType != null) {
                TestResult typeTest = declaringType.testInstance(type, ctx);
                if (typeTest == TestResult.NOT_PASSED) {
                    continue;
                } else if (typeTest == TestResult.DEFERRED) {
                    return typeTest;
                }
            }

            DeclaredType superType = (DeclaredType) type;

            List<ExecutableElement> methods = ElementFilter.methodsIn(superType.asElement().getEnclosedElements());

            for (ExecutableElement el : methods) {
                if (insp.overrides(declaration, el, methodDeclaringType)) {
                    return TestResult.PASSED;
                }
            }
        }

        return TestResult.NOT_PASSED;
    }

    private static List<TypeMirror> allSuperTypes(TypeMirror type, ModelInspector<?> insp) {
        List<TypeMirror> ret = new ArrayList<>();

        while (true) {
            List<? extends TypeMirror> directSuperTypes = insp.directSupertypes(type);
            if (directSuperTypes.isEmpty()) {
                break;
            }

            //we're interested in the super types, not implemented interfaces
            type = directSuperTypes.get(0);
            ret.add(type);
        }

        return ret;
    }
}
