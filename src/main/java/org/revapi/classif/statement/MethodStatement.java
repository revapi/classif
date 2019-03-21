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
package org.revapi.classif.statement;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.MethodConstraintsMatch;
import org.revapi.classif.match.declaration.MethodParameterMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.instance.TypeParametersMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.util.Glob;
import org.revapi.classif.util.Nullable;

public final class MethodStatement extends StatementStatement {
    private final NameMatch name;
    private final @Nullable TypeReferenceMatch returnType;
    private final @Nullable TypeReferenceMatch declaringType;
    private final @Nullable TypeParametersMatch typeParameters;
    private final @Nullable MethodConstraintsMatch constraints;
    private final Glob<MethodParameterMatch> parameters;

    private final boolean negation;

    public MethodStatement(@Nullable String definedVariable,
            List<String> referencedVariables, AnnotationsMatch annotations,
            ModifiersMatch modifiers, boolean isMatch, NameMatch name,
            @Nullable TypeReferenceMatch returnType, @Nullable TypeReferenceMatch declaringType,
            @Nullable TypeParametersMatch typeParameters,
            List<MethodParameterMatch> parameters,
            @Nullable MethodConstraintsMatch constraints, boolean negation) {
        super(definedVariable, referencedVariables, annotations, modifiers, isMatch);
        this.name = name;
        this.returnType = returnType;
        this.declaringType = declaringType;
        this.typeParameters = typeParameters;
        this.parameters = new Glob<>(parameters);
        this.constraints = constraints;
        this.negation = negation;
    }

    @Override
    protected ModelMatch createExactMatcher() {
        return new ModelMatch() {
            @Override
            public <M> TestResult testMethod(M method, MatchContext<M> ctx) {
                ExecutableElement element = (ExecutableElement) ctx.modelInspector.toElement(method);
                TypeMirror type = ctx.modelInspector.toMirror(method);

                TestResult res = TestResult.fromBoolean(name.matches(element.getSimpleName().toString()));

                if (modifiers != null) {
                    res = res.and(() -> modifiers.testDeclaration(element, type, ctx));
                }

                if (annotations != null) {
                    res = res.and(() -> annotations.testDeclaration(element, type, ctx));
                }

                if (returnType != null) {
                    res = res.and(() -> returnType.testInstance(element.getReturnType(), ctx));
                }

                res = res.and(() -> parameters.test((match, p) -> match.testDeclaration(p, p.asType(), ctx),
                        element.getParameters()));

                if (declaringType != null && res.toBoolean(false)) {
                    res = declaringType.testInstance(element.getEnclosingElement().asType(), ctx);
                }

                if (typeParameters != null && res.toBoolean(false)) {
                    res = typeParameters.testInstance(type, ctx);
                }

                if (constraints != null && res.toBoolean(false)) {
                    res = constraints.testDeclaration(element, type, ctx);
                }

                return negation ? res.negate() : res;
            }

            @Override
            public String toString() {
                StringBuilder bld = new StringBuilder();

                if (typeParameters != null) {
                    bld.append("<");
                    bld.append(typeParameters.toString());
                    bld.append(">");
                }

                if (returnType != null) {
                    if (bld.length() > 0) {
                        bld.append(" ");
                    }

                    bld.append(returnType);
                }

                if (declaringType != null) {
                    if (bld.length() > 0) {
                        bld.append(" ");
                    }

                    bld.append(declaringType);
                    bld.append("::");
                }

                if (isMatch()) {
                    bld.append("^");
                }

                insertVariable(bld);

                if (negation) {
                    bld.append("!");
                }

                bld.append(name);

                bld.append("(");
                bld.append(parameters.getMatches().stream().map(Object::toString).collect(Collectors.joining(", ")));
                bld.append(")");

                if (constraints != null) {
                    bld.append(" ");
                    bld.append(constraints);
                }

                return bld.toString();
            }
        };
    }
}
