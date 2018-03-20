package org.revapi.classif.statement;

import java.util.List;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.TypeKindMatch;
import org.revapi.classif.match.instance.AnnotationMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.TypeParametersMatch;

public class TypeDefinitionStatement extends StatementStatement {
    private final TypeKindMatch typeKind;
    private final FqnMatch fqn;
    private final TypeParametersMatch typeParameters;
    private final boolean negation;

    public TypeDefinitionStatement(String definedVariable, List<String> referencedVariables,
            List<AnnotationMatch> annotations,
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
                boolean ret = annotations.stream().allMatch(m -> m.test(type, ctx))
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
