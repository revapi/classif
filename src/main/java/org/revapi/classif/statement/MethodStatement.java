package org.revapi.classif.statement;

import java.util.List;

import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.util.Nullable;

public class MethodStatement extends StatementStatement {
    public MethodStatement(@Nullable String definedVariable,
            List<String> referencedVariables, AnnotationsMatch annotations,
            ModifiersMatch modifiers, boolean isMatch) {
        super(definedVariable, referencedVariables, annotations, modifiers, isMatch);
    }

    @Override
    protected ModelMatch createExactMatcher() {
        return null;
    }
}
