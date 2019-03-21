package org.revapi.classif.query.match.declaration;

import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.match.instance.FqnMatchBuilder;

public class AnnotationValueEnumMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilder<AnnotationValueEnumMatchBuilder<P>, AnnotationValueEnumMatchBuilder.EnumMatch, P> {

    private FqnMatch fqn;
    private NameMatch name;

    public AnnotationValueEnumMatchBuilder(P parent,
            BiConsumer<AnnotationValueEnumMatchBuilder<P>, EnumMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public FqnMatchBuilder<AnnotationValueEnumMatchBuilder<P>> fqn() {
        return new FqnMatchBuilder<>(this, (b, p) -> fqn = p);
    }

    public AnnotationValueEnumMatchBuilder<P> name(String name) {
        this.name = NameMatch.exact(name);
        return this;
    }

    public AnnotationValueEnumMatchBuilder<P> pattern(Pattern pattern) {
        this.name = NameMatch.pattern(pattern);
        return this;
    }

    public AnnotationValueEnumMatchBuilder<P> anyName() {
        this.name = NameMatch.any();
        return this;
    }


    public P endEnumConstant() {
        return finish(new EnumMatch(fqn, name));
    }

    public static final class EnumMatch {
        public final FqnMatch typeName;
        public final NameMatch name;

        private EnumMatch(FqnMatch typeName, NameMatch name) {
            this.typeName = typeName;
            this.name = name;
        }
    }
}
