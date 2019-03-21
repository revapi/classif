package org.revapi.classif.query.match.declaration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.AnnotationValueMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.util.Operator;

public class AnnotationValueArrayMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<AnnotationValueArrayMatchBuilder<P>, List<AnnotationValueMatch>, P> {
    private final List<AnnotationValueMatch> elementMatches = new ArrayList<>();

    public AnnotationValueArrayMatchBuilder(P parent,
            BiConsumer<AnnotationValueArrayMatchBuilder<P>, List<AnnotationValueMatch>> productConsumer) {
        super(parent, productConsumer);
    }

    public AnnotationValueMatchBuilder<AnnotationValueArrayMatchBuilder<P>> value() {
        return new AnnotationValueMatchBuilder<>(this, Operator.EQ, (b, p) -> {
            copyVariableReferences(b);
            elementMatches.add(p);
        });
    }

    public P endArray() {
        return finish(elementMatches);
    }
}
