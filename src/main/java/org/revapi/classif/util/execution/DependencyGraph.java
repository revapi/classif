package org.revapi.classif.util.execution;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.statement.AbstractStatement;

public final class DependencyGraph {
    private final Collection<Node<MatchExecutionContext>> allNodes;

    public DependencyGraph(List<String> namedMatches, List<AbstractStatement> statements) {
        allNodes = initMatches(namedMatches == null ? emptyList() : namedMatches,
                statements, new HashMap<>(), new HashMap<>());
    }

    public Collection<Node<MatchExecutionContext>> getAllNodes() {
        return allNodes;
    }

    private static Collection<Node<MatchExecutionContext>> initMatches(List<String> namedMatches,
            Collection<AbstractStatement> statements, Map<String, MatchExecutionContext> definers,
            Map<String, List<MatchExecutionContext>> referencers) {

        collectVariables(namedMatches, statements, definers, referencers);

        return createGraph(definers, referencers);
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
