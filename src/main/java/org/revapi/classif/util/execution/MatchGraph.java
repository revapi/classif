package org.revapi.classif.util.execution;

import java.util.Collection;

import org.revapi.classif.ModelInspector;

public class MatchGraph {

    /**
     * Ok, so this is a little bit non-trivial. The problem here is the following. While Java itself doesn't allow
     * any cyclic declarations, we extend the "constraints" a type/declared element needs to abide by by for example
     * the "uses" or "usedby" constraints, for which we even do not require any semantics - it is upon the user
     * supplied implementation of {@link ModelInspector} to determine what a "use" of a type is. We therefore cannot
     * guarantee that the graph of matches we are supplied here is free of cycles.
     *
     * <p>Now, in order to efficiently compute whether a given element matches the recipe provided by the user we
     * need to somehow "sort" the dependency graph so that we know that if parents of some match have been matched
     * for some element, we can proceed with testing further (or not). I.e. we need to apply a topological sort on
     * the graph where the nodes with the least incoming edges should go first. But a topological sort is only
     * possible for DAGs, not cyclic graphs.
     *
     * <p>Therefore we need to somehow break the cycles we potentially have in the graph and then sort it
     * topologically to come up with a match tree that we can then use for efficient checks.
     *
     * @param graph the graph of match nodes
     * @return a match tree for efficiently checking the elements
     */
    public static MatchExecution unwind(DependencyGraph graph) {
        UnwindingGraph<MatchExecutionContext> ug = new UnwindingGraph<>(graph.getAllNodes());
        Collection<Node<MatchExecutionContext>> unwound = ug.unwind();

        return new MatchExecution(unwound);
    }
}
