package org.revapi.classif.query.match.declaration;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.UsedByMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;

public class UsedByMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<UsedByMatchBuilder<P>, UsedByMatch, P> {
    private boolean directly;

    public UsedByMatchBuilder(P parent,
            BiConsumer<UsedByMatchBuilder<P>, UsedByMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public UsedByMatchBuilder<P> directly() {
        this.directly = true;
        return this;
    }

    public UsedByMatchBuilder<P> variables(String... variables) {
        this.referencedVariables.addAll(Arrays.asList(variables));
        return this;
    }

    public P endUsedBy() {
        return finish(new UsedByMatch(directly, referencedVariables));
    }
}
