package org.revapi.classif.query.match.instance;

import java.util.function.BiConsumer;

import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.SingleTypeReferenceMatch;
import org.revapi.classif.match.instance.TypeParametersMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;

public class SingleTypeReferenceMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<SingleTypeReferenceMatchBuilder<P>, SingleTypeReferenceMatch, P> {
    protected String variable;
    private FqnMatch fqn;
    private boolean negated;
    private TypeParametersMatch typeParams;
    private int arrayDimension;

    public SingleTypeReferenceMatchBuilder(P parent,
            BiConsumer<SingleTypeReferenceMatchBuilder<P>, SingleTypeReferenceMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public SingleTypeReferenceMatchBuilder<P> byVariable(String name) {
        this.variable = name;
        return this;
    }

    public FqnMatchBuilder<SingleTypeReferenceMatchBuilder<P>> fqn() {
        return new FqnMatchBuilder<>(this, (b, p) -> fqn = p);
    }

    public SingleTypeReferenceMatchBuilder<P> negated() {
        this.negated = true;
        return this;
    }

    public SingleTypeReferenceMatchBuilder<P> array(int dimension) {
        arrayDimension = dimension;
        return this;
    }

    public TypeParametersMatchBuilder<SingleTypeReferenceMatchBuilder<P>> typeParameters() {
        return new TypeParametersMatchBuilder<>(this, (b, p) -> {
            typeParams = p;
            copyVariableReferences(b);
        });
    }

    public P endType() {
        return finish(new SingleTypeReferenceMatch(fqn, typeParams, variable, negated, arrayDimension));
    }
}
