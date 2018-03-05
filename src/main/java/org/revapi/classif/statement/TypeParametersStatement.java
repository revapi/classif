/*
 * Copyright 2014-2018 Lukas Krejci
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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;

import org.revapi.classif.ModelInspector;

public final class TypeParametersStatement extends AbstractStatement {
    private final List<TypeParameterStatement> typeParameters;

    public TypeParametersStatement(List<TypeParameterStatement> typeParameters) {
        super(null, emptyList(), false);
        this.typeParameters = typeParameters;
    }

    @Override
    public AbstractMatcher createMatcher() {
        return new AbstractMatcher() {
            List<AbstractMatcher> tps = typeParameters.stream().map(TypeParameterStatement::createMatcher)
                    .collect(toList());

            @Override
            protected <E> boolean defaultElementTest(E element, ModelInspector<E> inspector,
                    Map<String, AbstractMatcher> variables) {
                return tps.stream().allMatch(tp -> tp.dispatchTest(element, inspector, variables));
            }
        };
    }
}
