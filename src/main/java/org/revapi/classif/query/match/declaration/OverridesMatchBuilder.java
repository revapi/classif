package org.revapi.classif.query.match.declaration;

import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.OverridesMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;
import org.revapi.classif.util.Nullable;

public class OverridesMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<OverridesMatchBuilder<P>, OverridesMatch, P> {
    private @Nullable TypeReferenceMatch fromType;

    public OverridesMatchBuilder(P parent,
            BiConsumer<OverridesMatchBuilder<P>, OverridesMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeReferenceMatchBuilder<OverridesMatchBuilder<P>> fromOneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            fromType = p;
        });
    }

    public P endOverrides() {
        return finish(new OverridesMatch(fromType));
    }
}
