package org.revapi.classif.match.instance;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import org.revapi.classif.match.MatchContext;

public class TypeParameterMatch extends TypeInstanceMatch {
    private final TypeParameterWildcardMatch wildcard;
    private final List<TypeReferenceMatch> bounds;

    public TypeParameterMatch(TypeParameterWildcardMatch wildcard, List<TypeReferenceMatch> bounds) {
        this.wildcard = wildcard;
        this.bounds = bounds;
    }

    public boolean isMatchAny() {
        if (wildcard != null || bounds.size() != 1) {
            return false;
        }

        TypeReferenceMatch ref = bounds.get(0);
        return ref.isMatchAny();
    }

    public boolean isMatchAll() {
        if (wildcard != null || bounds.size() != 1) {
            return false;
        }

        TypeReferenceMatch ref = bounds.get(0);
        return ref.isMatchAll();
    }

    @Override
    protected <M> boolean testIntersection(IntersectionType t, MatchContext<M> matchContext) {
        // each required bound has to be present in the actual bounds
        // there can be more actual bounds than required, but we still match
        nextBound: for (TypeReferenceMatch m : bounds) {
            for (TypeMirror b : t.getBounds()) {
                if (m.testInstance(b, matchContext)) {
                    continue nextBound;
                }
            }
            return false;
        }

        return true;
    }

    @Override
    protected <M> boolean testDeclared(DeclaredType t, MatchContext<M> matchContext) {
        return bounds.stream().allMatch(b -> b.testInstance(t, matchContext));
    }

    @Override
    protected <M> boolean testError(ErrorType t, MatchContext<M> matchContext) {
        return testDeclared(t, matchContext);
    }

    @Override
    protected <M> boolean testTypeVariable(TypeVariable t, MatchContext<M> matchContext) {
        return testInstance(t.getUpperBound(), matchContext);
    }

    @Override
    protected <M> boolean testWildcard(WildcardType t, MatchContext<M> matchContext) {
        return wildcard != null && wildcard.testInstance(t, matchContext);
    }
}
