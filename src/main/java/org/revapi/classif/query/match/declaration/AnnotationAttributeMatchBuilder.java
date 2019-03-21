package org.revapi.classif.query.match.declaration;

import java.util.function.BiConsumer;

import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.declaration.AnnotationAttributeMatch;
import org.revapi.classif.match.declaration.AnnotationValueMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.util.Operator;

public class AnnotationAttributeMatchBuilder<P extends AbstractBuilder<P, ?, ?>>
        extends AbstractBuilderWithReferencedVariables<AnnotationAttributeMatchBuilder<P>, AnnotationAttributeMatch, P> {

    private boolean any;
    private boolean all;
    private NameMatch name;
    private Operator operatorAgainstDefault;
    private AnnotationValueMatch value;

    public AnnotationAttributeMatchBuilder(P parent,
            BiConsumer<AnnotationAttributeMatchBuilder<P>, AnnotationAttributeMatch> productConsumer) {
        super(parent, productConsumer);
    }
    
    public AnnotationAttributeMatchBuilder<P> any() {
        this.any = true;
        return this;
    }

    public AnnotationAttributeMatchBuilder<P> all() {
        this.all = true;
        return this;
    }

    public AnnotationAttributeMatchBuilder<P> name(String name) {
        this.name = NameMatch.exact(name);
        return this;
    }

    public AnnotationValueMatchBuilder<AnnotationAttributeMatchBuilder<P>> value(Operator operator) {
        return new AnnotationValueMatchBuilder<>(this, operator, (b, p) -> {
            copyVariableReferences(b);
            value = p;
            if (value == null) {
                operatorAgainstDefault = operator;
            }
        });
    }

    public P endAttibute() {
        return finish(new AnnotationAttributeMatch(any, all, name, operatorAgainstDefault, value));
    }
}
