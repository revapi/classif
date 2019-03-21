package org.revapi.classif.query.match.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.query.AbstractBuilder;

public class FqnMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilder<FqnMatchBuilder<P>, FqnMatch, P> {

    private List<NameMatch> fqn = new ArrayList<>();

    public FqnMatchBuilder(P parent,
            BiConsumer<FqnMatchBuilder<P>, FqnMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public P fullyQualifiedName(String fqn) {
        String[] parts = fqn.split("\\.");
        for(String p : parts) {
            name(p);
        }

        return endFqn();
    }

    public FqnMatchBuilder<P> name(String name) {
        fqn.add(NameMatch.exact(name));
        return this;
    }

    public FqnMatchBuilder<P> pattern(String pattern) {
        fqn.add(NameMatch.pattern(Pattern.compile(pattern)));
        return this;
    }

    public FqnMatchBuilder<P> any() {
        fqn.add(NameMatch.any());
        return this;
    }

    public FqnMatchBuilder<P> all() {
        fqn.add(NameMatch.all());
        return this;
    }

    public P endFqn() {
        return finish(new FqnMatch(fqn));
    }
}
