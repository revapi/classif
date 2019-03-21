package org.revapi.classif.query.match.declaration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.AnnotationMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;

public class AnnotationsMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<AnnotationsMatchBuilder<P>, AnnotationsMatch, P> {

    private final List<AnnotationMatch> annotations = new ArrayList<>();

    public AnnotationsMatchBuilder(P parent,
            BiConsumer<AnnotationsMatchBuilder<P>, AnnotationsMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public AnnotationMatchBuilder<AnnotationsMatchBuilder<P>> annotation() {
        return new AnnotationMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            annotations.add(p);
        });
    }
    public P endAnnotations() {
        return finish(new AnnotationsMatch(annotations));
    }
}
