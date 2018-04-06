package org.revapi.classif.statement;

import static java.util.Collections.emptyList;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;

public final class GenericStatement extends StatementStatement {
    private final boolean negation;

    public GenericStatement(String definedVariable,
            AnnotationsMatch annotations, ModifiersMatch modifiers,
            boolean isMatch, boolean negation) {
        super(definedVariable, emptyList(), annotations, modifiers, isMatch);
        this.negation = negation;
    }

    @Override
    protected ModelMatch createExactMatcher() {
        return new ModelMatch() {
            @Override
            protected <M> boolean defaultElementTest(M model, MatchContext<M> ctx) {
                Element el = ctx.modelInspector.toElement(model);
                TypeMirror inst = ctx.modelInspector.toMirror(model);

                boolean ret = modifiers.test(el, inst, ctx) && annotations.test(el, inst, ctx);

                return negation != ret;
            }
        };
        // TODO add generic constraints
    }
}
