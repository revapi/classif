package org.revapi.classif.query.match.declaration;

import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.ModifierClusterMatch;
import org.revapi.classif.match.declaration.ModifierMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.Modifier;

public class ModifiersMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilder<ModifiersMatchBuilder<P>, ModifiersMatch, P> {
    private List<ModifierClusterMatch> clusters = new ArrayList<>();

    public ModifiersMatchBuilder(P parent,
            BiConsumer<ModifiersMatchBuilder<P>, ModifiersMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public ModifiersMatchBuilder<P> include(Modifier modifier) {
        clusters.add(new ModifierClusterMatch(singleton(new ModifierMatch(false, modifier.toString()))));
        return this;
    }

    public ModifiersMatchBuilder<P> includeAll(Modifier... modifiers) {
        for (Modifier mod : modifiers) {
            include(mod);
        }
        return this;
    }

    public ModifiersMatchBuilder<P> exclude(Modifier modifier) {
        clusters.add(new ModifierClusterMatch(singleton(new ModifierMatch(true, modifier.toString()))));
        return this;
    }

    public ModifiersMatchBuilder<P> excludeAll(Modifier... modifiers) {
        for (Modifier mod : modifiers) {
            exclude(mod);
        }
        return this;
    }

    public ModifierClusterMatchBuilder<ModifiersMatchBuilder<P>> atLeastOne() {
        return new ModifierClusterMatchBuilder<>(this, (b, p) -> clusters.add(p));
    }

    public P endModifiers() {
        return finish(new ModifiersMatch(clusters));
    }
}
