package org.revapi.classif.util.execution;

import java.util.Collections;
import java.util.List;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;

public final class MatchExecutionContext {
    public final String definedVariable;
    public final List<String> referencedVariables;
    public final boolean isReturn;
    public final ModelMatch match;

    MatchExecutionContext(String definedVariable, List<String> referencedVariables, boolean isReturn,
            ModelMatch match) {
        this.definedVariable = definedVariable;
        this.referencedVariables = Collections.unmodifiableList(referencedVariables);
        this.isReturn = isReturn;
        this.match = match;
    }
}
