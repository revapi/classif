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
import java.util.Map;
import java.util.Objects;

import javax.lang.model.element.TypeElement;

import org.revapi.classif.ModelInspector;

public final class TypeReferenceStatement extends AbstractStatement {
    private final FqnStatement fullyQualifiedName;
    private final TypeParametersStatement typeParameters;
    private final boolean negation;

    public TypeReferenceStatement(boolean negation, List<String> referencedVariables, FqnStatement fqn,
            TypeParametersStatement typeParams) {
        super(null, referencedVariables, false);
        this.fullyQualifiedName = fqn;
        this.typeParameters = typeParams;
        this.negation = negation;
    }

    @Override
    public AbstractMatcher createMatcher() {
        return new AbstractMatcher() {
            AbstractMatcher fqn = fullyQualifiedName == null ? null : fullyQualifiedName.createMatcher();
            AbstractMatcher tps = typeParameters == null ? null : typeParameters.createMatcher();

//            @Override
//            public <E> boolean testType(TypeElement type, ModelInspector<E> inspector,
//                    Map<String, AbstractMatcher> variables) {
//
//                boolean ret;
//
//                if (fqn != null) {
//                    ret = fqn.testType(type, inspector, variables);
//                    if (ret && tps != null) {
//                        ret = tps.testType(type, inspector, variables);
//                    }
//                } else {
//                    ret = getReferencedVariables().stream()
//                            .map(variables::get)
//                            .filter(Objects::nonNull)
//                            .anyMatch(m -> m.testType(type, inspector, variables));
//                }
//
//                return negation != ret;
//            }
        };
    }
}
