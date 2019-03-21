package org.revapi.classif.query.match.declaration;

import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.UsesMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;

public class UsesMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<UsesMatchBuilder<P>, UsesMatch, P> {
    private boolean directly;
    private TypeReferenceMatch ref;

    public UsesMatchBuilder(P parent, BiConsumer<UsesMatchBuilder<P>, UsesMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public UsesMatchBuilder<P> directly() {
        this.directly = true;
        return this;
    }

    public TypeReferenceMatchBuilder<UsesMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, r) -> {
            this.ref = r;
            copyVariableReferences(b);
        });
    }

    public P endUses() {
        UsesMatch match = new UsesMatch(directly, ref);
        return finish(match);
    }
}
