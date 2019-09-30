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
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.UsesMatch;
import org.revapi.classif.progress.StatementMatch;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.classif.util.Nullable;

public final class GenericStatement extends AbstractStatement {
    private final @Nullable UsesMatch usesMatch;

    public GenericStatement(@Nullable String definedVariable, List<String> referencedVariables,
            AnnotationsMatch annotations, ModifiersMatch modifiers,
            boolean isMatch, boolean negation, @Nullable UsesMatch usesMatch) {
        super(definedVariable, referencedVariables, isMatch, annotations, modifiers, negation);
        this.usesMatch = usesMatch;
    }

    @Override
    public <M> StatementMatch<M> createMatch() {
        return new StatementMatch<M>() {
            @Override
            protected TestResult defaultElementTest(M model, MatchContext<M> ctx) {
                Element el = ctx.getModelInspector().toElement(model);
                TypeMirror inst = ctx.getModelInspector().toMirror(model);

                TestResult ret = modifiers.test(el, inst, ctx).and(annotations.test(el, inst, ctx));
                if (usesMatch != null) {
                    ret = ret.and(usesMatch.test(el, inst, ctx));
                }

                return negation ? ret.negate() : ret;
            }

            @Override
            public String toString() {
                String ret = toStringPrefix() + (isMatch() ? "^" : "*");

                if (usesMatch != null) {
                    ret += usesMatch.toString();
                }

                return ret;
            }
        };
    }
}
