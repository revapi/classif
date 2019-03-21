package org.revapi.classif.query.match.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.instance.TypeParameterMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;

public class TypeParameterIntersectionMatchBuilder<P extends AbstractBuilder<P, ?, ?>>
        extends AbstractBuilderWithReferencedVariables<TypeParameterIntersectionMatchBuilder<P>, TypeParameterMatch, P> {

    private final List<TypeReferenceMatch> bounds = new ArrayList<>();

    protected TypeParameterIntersectionMatchBuilder(P parent,
            BiConsumer<TypeParameterIntersectionMatchBuilder<P>, TypeParameterMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeReferenceMatchBuilder<TypeParameterIntersectionMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            bounds.add(p);
        });
    }

    public P endIntersection() {
        return finish(new TypeParameterMatch(null, bounds));
    }
}
