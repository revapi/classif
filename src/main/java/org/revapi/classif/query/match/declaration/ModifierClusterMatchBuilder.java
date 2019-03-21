package org.revapi.classif.query.match.declaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.ModifierClusterMatch;
import org.revapi.classif.match.declaration.ModifierMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.Modifier;

public class ModifierClusterMatchBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractBuilder<ModifierClusterMatchBuilder<P>, ModifierClusterMatch, P> {
    private final Collection<ModifierMatch> mods = new ArrayList<>();

    public ModifierClusterMatchBuilder(P parent,
            BiConsumer<ModifierClusterMatchBuilder<P>, ModifierClusterMatch> productConsumer) {
        super(parent, productConsumer);
    }

    public ModifierClusterMatchBuilder<P> mod(Modifier mod) {
        mods.add(new ModifierMatch(false, mod.toString()));
        return this;
    }

    public ModifierClusterMatchBuilder<P> not(Modifier mod) {
        mods.add(new ModifierMatch(true, mod.toString()));
        return this;
    }

    public P endAtLeastOne() {
        return finish(new ModifierClusterMatch(mods));
    }
}
