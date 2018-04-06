package org.revapi.classif.statement;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.UsesMatch;

public final class GenericStatement extends StatementStatement {
    private final boolean negation;
    private final UsesMatch usesMatch;

    public GenericStatement(String definedVariable, List<String> referencedVariables,
            AnnotationsMatch annotations, ModifiersMatch modifiers,
            boolean isMatch, boolean negation, UsesMatch usesMatch) {
        super(definedVariable, referencedVariables, annotations, modifiers, isMatch);
        this.negation = negation;
        this.usesMatch = usesMatch;
    }

    @Override
    public boolean isDecidableInPlace() {
        return super.isDecidableInPlace() && usesMatch == null;
    }

    @Override
    protected ModelMatch createExactMatcher() {
        return new ModelMatch() {
            @Override
            protected <M> boolean defaultElementTest(M model, MatchContext<M> ctx) {
                Element el = ctx.modelInspector.toElement(model);
                TypeMirror inst = ctx.modelInspector.toMirror(model);

                boolean ret = modifiers.test(el, inst, ctx) && annotations.test(el, inst, ctx);
                if (usesMatch != null) {
                    ret = ret && usesMatch.test(el, inst, ctx);
                }

                return negation != ret;
            }
        };
    }
}