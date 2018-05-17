package org.revapi.classif.util;

import java.util.List;

import org.revapi.classif.match.ModelMatch;

final class UnprocessedMatch {
    final String definedVariable;
    final List<String> referencedVariables;
    final boolean isReturn;
    final ModelMatch match;

    UnprocessedMatch(String definedVariable, List<String> referencedVariables, boolean isReturn,
            ModelMatch match) {
        this.definedVariable = definedVariable;
        this.referencedVariables = referencedVariables;
        this.isReturn = isReturn;
        this.match = match;
    }
}
