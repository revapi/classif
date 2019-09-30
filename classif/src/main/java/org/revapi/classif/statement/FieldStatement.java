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

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.UsesMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.progress.StatementMatch;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.classif.util.Nullable;

public final class FieldStatement extends AbstractStatement {
    private final NameMatch name;
    private final @Nullable TypeReferenceMatch fieldType;
    private final @Nullable TypeReferenceMatch declaringType;
    private final @Nullable UsesMatch fieldConstraints;

    public FieldStatement(@Nullable String definedVariable,
            List<String> referencedVariables, AnnotationsMatch annotations,
            ModifiersMatch modifiers, boolean isMatch, boolean negation, NameMatch name,
            @Nullable TypeReferenceMatch fieldType,
            @Nullable TypeReferenceMatch declaringType, @Nullable UsesMatch fieldConstraints) {
        super(definedVariable, referencedVariables, isMatch, annotations, modifiers, negation);
        this.name = name;
        this.fieldType = fieldType;
        this.declaringType = declaringType;
        this.fieldConstraints = fieldConstraints;
    }

    @Override
    public <M> StatementMatch<M> createMatch() {
        return new StatementMatch<M>() {
            @Override
            public TestResult testVariable(M var, MatchContext<M> ctx) {
                Element element = ctx.getModelInspector().toElement(var);
                TypeMirror type = ctx.getModelInspector().toMirror(var);

                TestResult res = TestResult.fromBoolean(name.matches(element.getSimpleName().toString()));

                if (fieldType != null) {
                    res = res.and(fieldType.testInstance(type, ctx));
                }

                if (declaringType != null) {
                    res = res.and(declaringType.testInstance(element.getEnclosingElement().asType(), ctx));
                }

                if (fieldConstraints != null) {
                    res = res.and(fieldConstraints.testDeclaration(element, type, ctx));
                }

                return negation ? res.negate() : res;
            }

            @Override
            public String toString() {
                StringBuilder bld = new StringBuilder(toStringPrefix());
                if (fieldType != null) {
                    bld.append(fieldType.toString());
                }

                if (declaringType != null) {
                    if (bld.length() > 0) {
                        bld.append(" ");
                    }

                    bld.append(declaringType.toString());
                    bld.append("::");
                }

                if (isMatch()) {
                    if (bld.length() > 0) {
                        bld.append(" ");
                    }
                    bld.append("^");
                }

                insertVariable(bld);

                if (negation) {
                    bld.append("!");
                }

                bld.append(name.toString());

                if (fieldConstraints != null) {
                    bld.append(fieldConstraints.toString());
                }

                return bld.toString();
            }
        };
    }
}
