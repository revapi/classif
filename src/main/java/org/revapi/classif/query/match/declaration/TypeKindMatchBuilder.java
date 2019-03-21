package org.revapi.classif.query.match.declaration;

import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.TypeKindMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.TypeKind;

public class TypeKindMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilder<TypeKindMatchBuilder<P>, TypeKindMatch, P> {
    private boolean negated;
    private TypeKind kind;

    public TypeKindMatchBuilder(P parent,
            BiConsumer<TypeKindMatchBuilder<P>, TypeKindMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeKindMatchBuilder<P> negated() {
        negated = true;
        return this;
    }

    public TypeKindMatchBuilder<P> of(TypeKind kind) {
        this.kind = kind;
        return this;
    }

    public P endKind() {
        return finish(new TypeKindMatch(negated, kind.toString()));
    }
}
