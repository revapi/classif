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
package org.revapi.classif.util.execution;

import java.util.Collections;
import java.util.List;

import org.revapi.classif.match.ModelMatch;

/**
 * Holds the state necessary for the execution of a match in the {@link org.revapi.classif.MatchingProgress}.
 *
 * <p>The nodes in the {@link DependencyGraph} hold these contexts. As such a single execution context represents one
 * statement in the structural match.
 */
public final class MatchExecutionContext {
    /**
     * The variable defined by the structural match statement this context represents
     */
    public final String definedVariable;

    /**
     * The list of the variables referenced by the strutural match statement reprensented by this context
     */
    public final List<String> referencedVariables;

    /**
     * Whether or not this context represents a statement used as a return from the structural match
     */
    public final boolean isReturn;

    /**
     * The match object actually performing the matching operation on the model data
     */
    public final ModelMatch match;

    // package private so that this is not completely free for reuse.
    MatchExecutionContext(String definedVariable, List<String> referencedVariables, boolean isReturn,
            ModelMatch match) {
        this.definedVariable = definedVariable;
        this.referencedVariables = Collections.unmodifiableList(referencedVariables);
        this.isReturn = isReturn;
        this.match = match;
    }

    @Override
    public String toString() {
        return (isReturn ? "^" : "") + match.toString();
    }
}
