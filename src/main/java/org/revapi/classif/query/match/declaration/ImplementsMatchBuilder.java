package org.revapi.classif.query.match.declaration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.ImplementsMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;

public class ImplementsMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<ImplementsMatchBuilder<P>, ImplementsMatch, P> {
    private boolean directly;
    private boolean exactly;
    private final List<TypeReferenceMatch> types = new ArrayList<>();

    public ImplementsMatchBuilder(P parent,
            BiConsumer<ImplementsMatchBuilder<P>, ImplementsMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public ImplementsMatchBuilder<P> directly() {
        this.directly = true;
        return this;
    }

    public ImplementsMatchBuilder<P> exactly() {
        this.exactly = true;
        return this;
    }

    public TypeReferenceMatchBuilder<ImplementsMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            types.add(p);
        });
    }

    public P endImplements() {
        return finish(new ImplementsMatch(directly, exactly, types));
    }
}
