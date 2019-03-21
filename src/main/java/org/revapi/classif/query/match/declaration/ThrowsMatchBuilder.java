package org.revapi.classif.query.match.declaration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.ThrowsMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;

public class ThrowsMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<ThrowsMatchBuilder<P>, ThrowsMatch, P> {
    private final List<TypeReferenceMatch> types = new ArrayList<>();

    public ThrowsMatchBuilder(P parent,
            BiConsumer<ThrowsMatchBuilder<P>, ThrowsMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeReferenceMatchBuilder<ThrowsMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            types.add(p);
        });
    }

    public P endThrows() {
        return finish(new ThrowsMatch(types));
    }
}
