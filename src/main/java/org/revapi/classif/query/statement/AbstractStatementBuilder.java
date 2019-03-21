package org.revapi.classif.query.statement;

import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.declaration.AnnotationsMatchBuilder;
import org.revapi.classif.query.match.declaration.ModifiersMatchBuilder;

public abstract class AbstractStatementBuilder<
        This extends AbstractStatementBuilder<This, ProducedType, P>,
        ProducedType,
        P extends AbstractBuilder<P, ?, ?>>

        extends AbstractBuilderWithReferencedVariables<This, ProducedType, P> {

    protected String definedVariable;
    protected boolean returned;
    protected AnnotationsMatch annotations;
    protected ModifiersMatch modifiers;
    protected boolean negated;

    protected AbstractStatementBuilder(P parent, BiConsumer<This, ProducedType> productConsumer) {
        super(parent, productConsumer);
    }

    public This called(String variableName) {
        this.definedVariable = variableName;
        return castThis();
    }

    public This returned() {
        this.returned = true;
        return castThis();
    }

    public This negated() {
        this.negated = true;
        return castThis();
    }

    public AnnotationsMatchBuilder<This> annotations() {
        return new AnnotationsMatchBuilder<>(castThis(), (b, p) -> {
            copyVariableReferences(b);
            this.annotations = p;
        });
    }

    public ModifiersMatchBuilder<This> modifiers() {
        return new ModifiersMatchBuilder<>(castThis(), (b, ms) -> modifiers = ms);
    }

    public String getDefinedVariable() {
        return definedVariable;
    }

    public boolean isReturn() {
        return returned;
    }
}
