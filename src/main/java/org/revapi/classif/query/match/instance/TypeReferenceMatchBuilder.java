package org.revapi.classif.query.match.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.instance.SingleTypeReferenceMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.WithVariableReferences;

public class TypeReferenceMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends org.revapi.classif.query.AbstractBuilderWithReferencedVariables<TypeReferenceMatchBuilder<P>, TypeReferenceMatch, P>
        implements WithVariableReferences {

    private List<SingleTypeReferenceMatch> types = new ArrayList<>();

    public TypeReferenceMatchBuilder(P parent, BiConsumer<TypeReferenceMatchBuilder<P>, TypeReferenceMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeReferenceMatchBuilder<P> any() {
        return type().fqn().any().endFqn().endType();
    }

    public TypeReferenceMatchBuilder<P> all() {
        return type().fqn().all().endFqn().endType();
    }

    public SingleTypeReferenceMatchBuilder<TypeReferenceMatchBuilder<P>> type() {
        return new SingleTypeReferenceMatchBuilder<>(castThis(), (b, p) -> {
            types.add(p);
            if (b.variable != null) {
                referencedVariables.add(b.variable);
            }
        });
    }

    public P endOneOfTypes() {
        return finish(new TypeReferenceMatch(types));
    }
}
