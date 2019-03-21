package org.revapi.classif.query.match.instance;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.instance.TypeParameterMatch;
import org.revapi.classif.match.instance.TypeParametersMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;

public class TypeParametersMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<TypeParametersMatchBuilder<P>, TypeParametersMatch, P> {
    private final List<TypeParameterMatch> typeParams = new ArrayList<>();

    public TypeParametersMatchBuilder(P parent,
            BiConsumer<TypeParametersMatchBuilder<P>, TypeParametersMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeParametersMatchBuilder<P> any() {
        return bound().oneOfTypes().type().fqn().any().endFqn().endType().endOneOfTypes().endBound();
    }

    public TypeParametersMatchBuilder<P> all() {
        return bound().oneOfTypes().type().fqn().all().endFqn().endType().endOneOfTypes().endBound();
    }

    public TypeParameterWildcardMatchBuilder<TypeParametersMatchBuilder<P>> wildcard() {
        return new TypeParameterWildcardMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            typeParams.add(new TypeParameterMatch(p, null));
        });
    }

    public TypeParameterBoundMatchBuilder<TypeParametersMatchBuilder<P>> bound() {
        return new TypeParameterBoundMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            typeParams.add(p);
        });
    }

    public TypeParameterIntersectionMatchBuilder<TypeParametersMatchBuilder<P>> intersection() {
        return new TypeParameterIntersectionMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            typeParams.add(p);
        });
    }

    public P endTypeParameters() {
        return finish(new TypeParametersMatch(typeParams));
    }
}
