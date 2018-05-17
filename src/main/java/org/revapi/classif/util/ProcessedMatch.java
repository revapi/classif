package org.revapi.classif.util;

import java.util.Collections;
import java.util.Map;

import org.revapi.classif.ModelInspector;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;

public final class ProcessedMatch extends TreeNode<ProcessedMatch> {
    private final boolean isReturn;
    private final ModelMatch match;

    ProcessedMatch(ModelMatch match, boolean isReturn) {
        this.match = match;
        this.isReturn = isReturn;
    }

    public <M> MatchNode<M> freeze(ModelInspector<M> inspector) {
        MatchNode<M> me = new MatchNode<>(match, isReturn, new MatchContext<>(inspector, getVariables()));
        getChildren().forEach(child -> me.getChildren().add(child.freeze(inspector)));

        return me;
    }

    private Map<String, ModelMatch> getVariables() {
        // TODO implement
        return Collections.emptyMap();
    }
}
