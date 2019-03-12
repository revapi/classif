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

import static org.revapi.classif.TestResult.TestableStream.testable;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.util.Nullable;
import org.revapi.classif.util.TreeNode;
import org.revapi.classif.match.Match;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;

public abstract class AbstractStatement extends TreeNode<AbstractStatement> {
    private final String definedVariable;
    private final List<String> referencedVariables;
    private final List<Match> innerMatchers;
    private final boolean isMatch;

    protected AbstractStatement(@Nullable String definedVariable, List<String> referencedVariables,
            List<Match> innerMatchers, boolean isMatch) {
        this.definedVariable = definedVariable;
        this.referencedVariables = referencedVariables;
        this.innerMatchers = innerMatchers;
        this.isMatch = isMatch;
    }

    public String getDefinedVariable() {
        return definedVariable;
    }

    public List<String> getReferencedVariables() {
        return referencedVariables;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public final ModelMatch createMatcher() {
        return new ModelMatch() {
            ModelMatch exact = createExactMatcher();

            @Override
            protected <M> TestResult defaultElementTest(M model, MatchContext<M> ctx) {
                Element el = ctx.modelInspector.toElement(model);
                TypeMirror t = ctx.modelInspector.toMirror(model);

                return testable(innerMatchers).testAll(m -> m.test(el, t, ctx)).and(() -> exact.test(model, ctx));
            }

            @Override
            public String toString() {
                String prefix = toStringPrefix();

                return prefix + " " + exact.toString();
            }
        };
    }

    protected abstract ModelMatch createExactMatcher();

    protected abstract String toStringPrefix();

    protected void insertVariable(StringBuilder bld) {
        if (definedVariable != null) {
            if (bld.length() > 0) {
                bld.append(" ");
            }
            bld.append("%").append(definedVariable).append("=");
        }
    }

    @Override
    public String toString() {
        return createMatcher().toString();
    }
}
