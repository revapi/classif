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

import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.classif.progress.StatementMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.TypeConstraintsMatch;
import org.revapi.classif.match.declaration.TypeKindMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.TypeParametersMatch;
import org.revapi.classif.util.Nullable;

public final class TypeDefinitionStatement extends AbstractStatement {
    private final TypeKindMatch typeKind;
    private final FqnMatch fqn;
    private final @Nullable TypeParametersMatch typeParameters;
    private final @Nullable TypeConstraintsMatch constraints;

    public TypeDefinitionStatement(@Nullable String definedVariable, List<String> referencedVariables,
            AnnotationsMatch annotations,
            ModifiersMatch modifiers,
            TypeKindMatch typeKind,
            FqnMatch fqn,
            @Nullable TypeParametersMatch typeParameters,
            @Nullable TypeConstraintsMatch constraints,
            boolean negation,
            boolean isMatch) {
        super(definedVariable, referencedVariables, isMatch, annotations, modifiers, negation);
        this.typeKind = typeKind;
        this.fqn = fqn;
        this.typeParameters = typeParameters;
        this.constraints = constraints;
    }

    @Override
    public <M> StatementMatch<M> createMatch() {
        return new StatementMatch<M>() {
            @Override
            public TestResult testType(M type, MatchContext<M> ctx) {

                TestResult ret = annotations.test(type, ctx)
                        .and(() -> modifiers.test(type, ctx))
                        .and(() -> typeKind.test(type, ctx))
                        .and(() -> fqn.test(type, ctx));

                if (typeParameters != null) {
                    ret = ret.and(() -> typeParameters.test(type, ctx));
                }

                if (constraints != null) {
                    ret = ret.and(() -> constraints.test(type, ctx));
                }

                return negation ? ret.negate() : ret;
            }

            @Override
            public String toString() {
                StringBuilder bld = new StringBuilder(toStringPrefix());

                bld.append(typeKind.toString()).append(" ");

                if (isMatch()) {
                    bld.append("^");
                }

                insertVariable(bld);

                if (negation) {
                    bld.append("!");
                }

                bld.append(fqn);

                if (typeParameters != null) {
                    bld.append("<");
                    bld.append(typeParameters);
                    bld.append(">");
                }

                if (constraints != null) {
                    bld.append(" ");
                    bld.append(constraints);
                }

                return bld.toString();
            }
        };
    }
}
