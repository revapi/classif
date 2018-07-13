package org.revapi.classif.statement;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.UsesMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.util.Nullable;

public class FieldStatement extends StatementStatement {
    private final boolean negation;
    private final NameMatch name;
    private final @Nullable TypeReferenceMatch fieldType;
    private final @Nullable TypeReferenceMatch declaringType;
    private final @Nullable UsesMatch fieldConstraints;

    public FieldStatement(@Nullable String definedVariable,
            List<String> referencedVariables, AnnotationsMatch annotations,
            ModifiersMatch modifiers, boolean isMatch, boolean negation, NameMatch name,
            @Nullable TypeReferenceMatch fieldType,
            @Nullable TypeReferenceMatch declaringType, @Nullable UsesMatch fieldConstraints) {
        super(definedVariable, referencedVariables, annotations, modifiers, isMatch);
        this.negation = negation;
        this.name = name;
        this.fieldType = fieldType;
        this.declaringType = declaringType;
        this.fieldConstraints = fieldConstraints;
    }

    @Override
    protected ModelMatch createExactMatcher() {
        return new ModelMatch() {
            @Override
            public <M> TestResult testVariableUndecidedly(M var, MatchContext<M> ctx) {
                Element element = ctx.modelInspector.toElement(var);
                TypeMirror type = ctx.modelInspector.toMirror(var);

                TestResult res = TestResult.fromBoolean(name.matches(element.getSimpleName().toString()));

                if (res.toBoolean(true) && fieldType != null) {
                    res = fieldType.testInstance(type, ctx);
                }

                if (res.toBoolean(true) && declaringType != null) {
                    res = declaringType.testInstance(element.getEnclosingElement().asType(), ctx);
                }

                if (res.toBoolean(true) && fieldConstraints != null) {
                    res = fieldConstraints.testDeclaration(element, type, ctx);
                }

                return negation ? res.negate() : res;
            }
        };
    }
}
