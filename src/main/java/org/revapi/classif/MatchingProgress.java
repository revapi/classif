package org.revapi.classif;

import java.util.Collections;
import java.util.Map;

import org.revapi.classif.util.MatchNode;

public final class MatchingProgress<M> {
    private final MatchNode<M> matchTree;

    MatchingProgress(MatchNode<M> matchTree) {
        this.matchTree = matchTree;
    }

    public TestResult test(M model) {
        return matchTree.test(model);
    }

    public Map<M, TestResult> finish() {
        // TODO implement
        return Collections.emptyMap();
    }
}
