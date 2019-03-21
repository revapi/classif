package org.revapi.classif.query.match.declaration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.AnnotationAttributeMatch;
import org.revapi.classif.match.declaration.AnnotationMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;

public class AnnotationMatchBuilder<P extends AbstractBuilder<P, ?, ?>>
        extends AbstractBuilderWithReferencedVariables<AnnotationMatchBuilder<P>, AnnotationMatch, P> {
    private boolean negation;
    private TypeReferenceMatch type;
    private final List<AnnotationAttributeMatch> attributes = new ArrayList<>();

    public AnnotationMatchBuilder(P parent,
            BiConsumer<AnnotationMatchBuilder<P>, AnnotationMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public AnnotationMatchBuilder<P> negated() {
        this.negation = true;
        return this;
    }

    public AnnotationAttributeMatchBuilder<AnnotationMatchBuilder<P>> attribute() {
        return new AnnotationAttributeMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            attributes.add(p);
        });
    }

    public AnnotationTypeMatchBuilder<AnnotationMatchBuilder<P>> type() {
        return new AnnotationTypeMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            this.type = p;
        });
    }

    public P endAnnotation() {
        return finish(new AnnotationMatch(negation, type, attributes));
    }
}
