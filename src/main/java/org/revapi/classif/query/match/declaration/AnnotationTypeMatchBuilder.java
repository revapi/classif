package org.revapi.classif.query.match.declaration;

import java.util.function.BiConsumer;

import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;

public class AnnotationTypeMatchBuilder<P extends AbstractBuilder<P, ?, ?>>
        extends AbstractBuilderWithReferencedVariables<AnnotationTypeMatchBuilder<P>, TypeReferenceMatch, P> {

    private TypeReferenceMatch type;
    public AnnotationTypeMatchBuilder(P parent,
            BiConsumer<AnnotationTypeMatchBuilder<P>, TypeReferenceMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeReferenceMatchBuilder<AnnotationTypeMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            this.type = p;
        });
    }

    public P endType() {
        return finish(type);
    }
}
