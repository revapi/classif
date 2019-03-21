package org.revapi.classif.query.match.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.instance.TypeParameterWildcardMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;

public class TypeParameterWildcardMatchBuilder<P extends AbstractBuilder<P, ?, ?>>
        extends AbstractBuilderWithReferencedVariables<TypeParameterWildcardMatchBuilder<P>, TypeParameterWildcardMatch, P> {

    private boolean isExtends = true;
    private final List<TypeReferenceMatch> bounds = new ArrayList<>();

    protected TypeParameterWildcardMatchBuilder(P parent,
            BiConsumer<TypeParameterWildcardMatchBuilder<P>, TypeParameterWildcardMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeParameterWildcardMatchBuilder<P> lower() {
        this.isExtends = false;
        return this;
    }

    public TypeParameterWildcardMatchBuilder<P> upper() {
        this.isExtends = true;
        return this;
    }

    public TypeReferenceMatchBuilder<TypeParameterWildcardMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            bounds.add(p);
        });
    }

    public P endWildcard() {
        return finish(new TypeParameterWildcardMatch(isExtends, bounds));
    }
}
