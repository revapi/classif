package org.revapi.classif.util.execution;

import java.util.Collection;

public class MatchExecution {
    private final Collection<Node<MatchExecutionContext>> matches;

    public MatchExecution(Collection<Node<MatchExecutionContext>> matches) {
        this.matches = matches;
    }

    public Collection<Node<MatchExecutionContext>> getMatches() {
        return matches;
    }

    //TODO define
}
