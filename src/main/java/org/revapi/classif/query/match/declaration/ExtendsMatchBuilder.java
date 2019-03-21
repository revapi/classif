package org.revapi.classif.query.match.declaration;

import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.ExtendsMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;

public class ExtendsMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<ExtendsMatchBuilder<P>, ExtendsMatch, P> {
    private boolean directly;
    private TypeReferenceMatch superType;

    public ExtendsMatchBuilder(P parent,
            BiConsumer<ExtendsMatchBuilder<P>, ExtendsMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public ExtendsMatchBuilder<P> directly() {
        this.directly = true;
        return this;
    }

    public TypeReferenceMatchBuilder<ExtendsMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            this.superType = p;
        });
    }

    public P endExtends() {
        return finish(new ExtendsMatch(directly, superType));
    }
}
