package org.revapi.classif.query.match.declaration;

import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.MethodParameterMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;
import org.revapi.classif.util.Nullable;

public class MethodParameterMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilderWithReferencedVariables<MethodParameterMatchBuilder<P>, MethodParameterMatch, P> {
    private @Nullable AnnotationsMatch annotations;
    private @Nullable TypeReferenceMatch type;

    public MethodParameterMatchBuilder(P parent,
            BiConsumer<MethodParameterMatchBuilder<P>, MethodParameterMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeReferenceMatchBuilder<MethodParameterMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            type = p;
        });
    }

    public AnnotationsMatchBuilder<MethodParameterMatchBuilder<P>> annotations() {
        return new AnnotationsMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            annotations = p;
        });
    }

    public P endParameter() {
        return finish(new MethodParameterMatch(annotations, type));
    }
}
