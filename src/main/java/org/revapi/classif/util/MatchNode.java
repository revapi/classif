package org.revapi.classif.util;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;

public final class MatchNode<M> extends TreeNode<MatchNode<M>> {
    private final boolean isReturn;
    private final ModelMatch match;
    private final MatchContext<M> matchContext;

    MatchNode(ModelMatch match, boolean isReturn, MatchContext<M> context) {
        this.match = match;
        this.isReturn = isReturn;
        this.matchContext = context;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public TestResult test(M model) {
        // TODO this is more complex due to the hierarchical nature of things
        return TestResult.fromBoolean(match.test(model, matchContext));
    }
}
