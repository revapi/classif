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
package org.revapi.classif.statement;

import java.util.List;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.TypeKindMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.TypeParametersMatch;

public class TypeDefinitionStatement extends StatementStatement {
    private final TypeKindMatch typeKind;
    private final FqnMatch fqn;
    private final TypeParametersMatch typeParameters;
    private final boolean negation;

    public TypeDefinitionStatement(String definedVariable, List<String> referencedVariables,
            AnnotationsMatch annotations,
            ModifiersMatch modifiers,
            TypeKindMatch typeKind,
            FqnMatch fqn,
            TypeParametersMatch typeParameters,
            boolean negation,
            boolean isMatch) {
        super(definedVariable, referencedVariables, annotations, modifiers, isMatch);
        this.typeKind = typeKind;
        this.fqn = fqn;
        this.typeParameters = typeParameters;
        this.negation = negation;
    }

    @Override
    protected ModelMatch createExactMatcher() {
        return new ModelMatch() {
            @Override
            public <M> boolean testType(M type, MatchContext<M> ctx) {
                boolean ret = annotations.test(type, ctx)
                        && modifiers.test(type, ctx)
                        && typeKind.test(type, ctx)
                        && fqn.test(type, ctx);

                if (typeParameters != null) {
                    ret = typeParameters.test(type, ctx);
                }

                return negation != ret;
            }
        };
    }
}
