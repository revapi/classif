package org.revapi.classif.util;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.revapi.classif.ModelInspector;
import org.revapi.classif.util.DependencyGraph.Node;

public class MatchTree {

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
    public static ProcessedMatch unwind(DependencyGraph graph) {
        Set<List<Node<UnprocessedMatch>>> cycles = detectCycles(graph.getAllNodes());
        breakCycles(graph.getAllNodes(), cycles);
        return sort(graph.getAllNodes());
    }

    private static Set<List<Node<UnprocessedMatch>>> detectCycles(Collection<Node<UnprocessedMatch>> graph) {
        Set<List<Node<UnprocessedMatch>>> cycles = new HashSet<>();
        ArrayList<Node<UnprocessedMatch>> traversal = new ArrayList<>();
        Set<Node<UnprocessedMatch>> processed = new HashSet<>();

        graph.forEach(n -> detectCycles(n, cycles, traversal, processed));

        return cycles;
    }

    private static void detectCycles(Node<UnprocessedMatch> node, Set<List<Node<UnprocessedMatch>>> cycles,
            List<Node<UnprocessedMatch>> currentTraversal, Set<Node<UnprocessedMatch>> processed) {

        if (processed.contains(node)) {
            return;
        }

        processed.add(node);

        if (currentTraversal.contains(node)) {
            if (node.out.contains(node)) {
                cycles.add(Arrays.asList(node, node));
            }

            currentTraversal.add(node);
            cycles.add(new ArrayList<>(currentTraversal));
        } else {
            currentTraversal.add(node);

            for (Node<UnprocessedMatch> child : node.out) {
                if (child.equals(node)) {
                    cycles.add(Arrays.asList(node, node));
                }
                detectCycles(child, cycles, currentTraversal, processed);
            }
        }

        currentTraversal.remove(currentTraversal.size() - 1);
    }

    private static void breakCycles(Collection<Node<UnprocessedMatch>> graph, Set<List<Node<UnprocessedMatch>>> cycles) {
        IdentityHashMap<Node<UnprocessedMatch>, Set<Node<UnprocessedMatch>>> clusters = new IdentityHashMap<>();
        // TODO implement
    }

    private static ProcessedMatch sort(Collection<Node<UnprocessedMatch>> dag) {
        ProcessedMatch ret = new ProcessedMatch(null, false);

        Set<Node<UnprocessedMatch>> roots = dag.stream().filter(n -> n.in.isEmpty()).collect(toSet());

        roots.forEach(n -> convert(n, ret));

        return ret;
    }

    private static void convert(Node<UnprocessedMatch> node, ProcessedMatch parent) {
        ProcessedMatch matchNode = new ProcessedMatch(node.node.match, node.node.isReturn);

        node.out.forEach(child -> convert(child, matchNode));

        parent.getChildren().add(matchNode);
    }

    private static final class NodeGroup<T> {
        final Node<T> original;
        final Set<Node<T>> splits = new HashSet<>(4);

        public NodeGroup(Node<T> original) {
            this.original = original;
        }
    }
}
