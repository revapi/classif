package org.revapi.classif.query.match.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.instance.TypeParameterMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;

public class TypeParameterBoundMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<TypeParameterBoundMatchBuilder<P>, TypeParameterMatch, P> {
    private final List<TypeReferenceMatch> bounds = new ArrayList<>();

    public TypeParameterBoundMatchBuilder(P parent,
            BiConsumer<TypeParameterBoundMatchBuilder<P>, TypeParameterMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeReferenceMatchBuilder<TypeParameterBoundMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            bounds.add(p);
        });
    }
    public P endBound() {
        return finish(new TypeParameterMatch(null, bounds));
    }
}
