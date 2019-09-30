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

import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.progress.StatementMatch;
import org.revapi.classif.progress.context.StatementContext;
import org.revapi.classif.util.Nullable;
import org.revapi.classif.util.TreeNode;

public abstract class AbstractStatement extends TreeNode<AbstractStatement> {
    protected final AnnotationsMatch annotations;
    protected final ModifiersMatch modifiers;
    protected final boolean negation;
    private final String definedVariable;
    private final List<String> referencedVariables;
    private final boolean isMatch;

    protected AbstractStatement(@Nullable String definedVariable, List<String> referencedVariables,
            boolean isMatch, AnnotationsMatch annotations, ModifiersMatch modifiers, boolean negation) {
        this.definedVariable = definedVariable;
        this.referencedVariables = referencedVariables;
        this.isMatch = isMatch;
        this.annotations = annotations;
        this.modifiers = modifiers;
        this.negation = negation;
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

    public abstract <M> StatementMatch<M> createMatch();

    public final <M> StatementMatch<M> createMatch(StatementContext<M> ctx) {
        StatementMatch<M> m = createMatch();
        m.setContext(ctx);
        return m;
    }

    protected String toStringPrefix() {
        return annotations.toString() + (annotations.isEmpty() ? "" : " ")
                + modifiers.toString() + (modifiers.isEmpty() ? "" : " ");
    }

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
        return createMatch().toString();
    }
}
