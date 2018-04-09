package org.revapi.classif.match.declaration;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.match.MatchContext;

public final class TypeConstraintsMatch extends DeclarationMatch {
    private final List<ImplementsMatch> implemented;
    private final List<UsesMatch> uses;

    public TypeConstraintsMatch(List<ImplementsMatch> implemented, List<UsesMatch> uses) {
        this.implemented = implemented;
        this.uses = uses;
    }

    public boolean isDecidableInPlace() {
        return uses == null;
    }

    @Override
    public <M> boolean testDeclaration(Element declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        // TODO add the rest of the type constraints
        return implemented.stream().allMatch(m -> m.testDeclaration(declaration, instantiation, ctx))
                && uses.stream().allMatch(m -> m.testDeclaration(declaration, instantiation, ctx));
    }
}
