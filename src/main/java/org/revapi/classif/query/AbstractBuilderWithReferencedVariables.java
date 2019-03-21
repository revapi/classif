package org.revapi.classif.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractBuilderWithReferencedVariables<This extends AbstractBuilderWithReferencedVariables<This, ProducedType, P>, ProducedType, P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilder<This, ProducedType, P> implements WithVariableReferences {

    protected final List<String> referencedVariables = new ArrayList<>();

    protected AbstractBuilderWithReferencedVariables(P parent,
            BiConsumer<This, ProducedType> productConsumer) {
        super(parent, productConsumer);
    }

    @Override
    public List<String> getVariableReferences() {
        return referencedVariables;
    }

    protected void copyVariableReferences(WithVariableReferences other) {
        this.referencedVariables.addAll(other.getVariableReferences());
    }
}
