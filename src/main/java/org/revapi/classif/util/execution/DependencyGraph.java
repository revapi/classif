package org.revapi.classif.util.execution;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.statement.AbstractStatement;

public final class DependencyGraph {
    private final Collection<Node<MatchExecutionContext>> allNodes;

    /**
     * Creates a directed acyclic graph of dependent matchers from the provided statements
     *
     * @param namedMatches the names of the matches to return as result of the whole structural match
     * @param statements the statements of the structural match
     * @throws IllegalArgumentException if the statements contain dependency cycles (dependencies are caused by variables)
     */
    public DependencyGraph(List<String> namedMatches, List<AbstractStatement> statements) throws IllegalArgumentException {
        allNodes = initMatches(namedMatches == null ? emptyList() : namedMatches,
                statements, new HashMap<>(), new HashMap<>());
    }

    /**
     * @return the nodes of the graph to support the matching progress
     */
    public Collection<Node<MatchExecutionContext>> getAllNodes() {
        return allNodes;
    }

    private static Collection<Node<MatchExecutionContext>> initMatches(List<String> namedMatches,
            Collection<AbstractStatement> statements, Map<String, MatchExecutionContext> definers,
            Map<String, List<MatchExecutionContext>> referencers) {

        collectVariables(namedMatches, statements, definers, referencers);

        Collection<Node<MatchExecutionContext>> ret = createGraph(definers, referencers);

        if (isCyclic(ret)) {
            throw new IllegalArgumentException("The statements' variables create a cyclic graph. This is not supported.");
        }

        return ret;
    }

    private static <T> boolean isCyclic(Collection<Node<T>> nodes) {
        return nodes.stream().map(n -> isCyclic(n, new HashSet<>())).reduce(false, Boolean::logicalOr);
    }

    private static boolean isCyclic(Node<?> node, Set<Node<?>> currentTraversal) {
        if (currentTraversal.contains(node)) {
            return true;
        }

        currentTraversal.add(node);
        boolean ret = false;

        for (Node<?> child : node.out()) {
            if (isCyclic(child, currentTraversal)) {
                ret = true;
                break;
            }
        }

        currentTraversal.remove(node);

        return ret;
    }

    private static void collectVariables(List<String> namedMatches, Collection<AbstractStatement> statements,
            Map<String, MatchExecutionContext> definers, Map<String, List<MatchExecutionContext>> referencers) {

        for (AbstractStatement st : statements) {
            ModelMatch stMatcher = st.createMatcher();

            MatchExecutionContext match = new MatchExecutionContext(st.getDefinedVariable(),
                    st.getReferencedVariables(), st.isMatch() || namedMatches.contains(st.getDefinedVariable()),
                    stMatcher);

            if (st.getDefinedVariable() != null) {
                definers.put(st.getDefinedVariable(), match);
            }

            st.getReferencedVariables()
                    .forEach(v -> referencers.computeIfAbsent(v, __ -> new ArrayList<>()).add(match));

            collectVariables(namedMatches, st.getChildren(), definers, referencers);
        }
    }

    private static Collection<Node<MatchExecutionContext>> createGraph(Map<String, MatchExecutionContext> definers,
            Map<String, List<MatchExecutionContext>> referencers) {

        Map<MatchExecutionContext, Node<MatchExecutionContext>> cache = new HashMap<>();

        definers.forEach((name, match) -> {
            Node<MatchExecutionContext> node = cache.computeIfAbsent(match, Node::new);

            referencers.getOrDefault(name, emptyList()).forEach(ref -> {
                Node<MatchExecutionContext> refNode = cache.computeIfAbsent(ref, Node::new);
                node.out().add(refNode);
                refNode.in().add(node);
            });
        });

        return cache.values();
    }
}
