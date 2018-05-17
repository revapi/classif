package org.revapi.classif.util;

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
    private final Collection<Node<UnprocessedMatch>> allNodes;

    public DependencyGraph(List<String> namedMatches, List<AbstractStatement> statements) {
        allNodes = initMatches(namedMatches == null ? emptyList() : namedMatches,
                statements, new HashMap<>(), new HashMap<>());
    }

    public Collection<Node<UnprocessedMatch>> getAllNodes() {
        return allNodes;
    }

    private static Collection<Node<UnprocessedMatch>> initMatches(List<String> namedMatches,
            Collection<AbstractStatement> statements, Map<String, UnprocessedMatch> definers,
            Map<String, List<UnprocessedMatch>> referencers) {

        collectVariables(namedMatches, statements, definers, referencers);

        return createGraph(definers, referencers);
    }

    private static void collectVariables(List<String> namedMatches, Collection<AbstractStatement> statements,
            Map<String, UnprocessedMatch> definers, Map<String, List<UnprocessedMatch>> referencers) {

        for (AbstractStatement st : statements) {
            ModelMatch stMatcher = st.createMatcher();

            UnprocessedMatch match = new UnprocessedMatch(st.getDefinedVariable(),
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

    private static Collection<Node<UnprocessedMatch>> createGraph(Map<String, UnprocessedMatch> definers,
            Map<String, List<UnprocessedMatch>> referencers) {

        Map<UnprocessedMatch, Node<UnprocessedMatch>> cache = new HashMap<>();

        definers.forEach((name, match) -> {
            Node<UnprocessedMatch> node = cache.computeIfAbsent(match, Node::new);

            referencers.getOrDefault(name, emptyList()).forEach(ref -> {
                Node<UnprocessedMatch> refNode = cache.computeIfAbsent(ref, Node::new);
                node.out.add(refNode);
                refNode.in.add(node);
            });
        });

        return cache.values();
    }

    static final class Node<T> {
        final T node;
        final Set<Node<T>> out;
        final Set<Node<T>> in;

        Node(T node) {
            this.node = node;
            out = new HashSet<>();
            in = new HashSet<>();
        }
    }
}
