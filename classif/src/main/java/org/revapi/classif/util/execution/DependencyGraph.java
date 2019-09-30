/*
 * Copyright 2018-2019 Lukas Krejci
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.revapi.classif.util.execution;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.revapi.classif.statement.AbstractStatement;

/**
 * This is a support class for {@link org.revapi.classif.StructuralMatcher} and
 * {@link org.revapi.classif.progress.MatchingProgress} that converts the variables and statements of a structural match
 * into a graph of dependent nodes.
 */
public final class DependencyGraph {
    private final Collection<Node<StatementWrapper>> allNodes;

    /**
     * Creates a directed acyclic graph of dependent matchers from the provided statements
     *
     * @param namedMatches the names of the matches to return as result of the whole structural match
     * @param statements the statements of the structural match
     * @throws IllegalArgumentException if the statements contain dependency cycles (dependencies are caused by variables)
     */
    public DependencyGraph(List<String> namedMatches, List<AbstractStatement> statements)
            throws IllegalArgumentException {
        allNodes = initMatches(namedMatches == null ? emptyList() : namedMatches,
                statements, new HashMap<>(), new HashMap<>());
    }

    /**
     * @return the nodes of the graph to support the matching progress
     */
    public Collection<Node<StatementWrapper>> getAllNodes() {
        return allNodes;
    }

    @Override
    public String toString() {
        return allNodes.stream().filter(n -> n.getParent() == null).map(Object::toString).collect(Collectors.joining("\n\n"));
    }

    private static Collection<Node<StatementWrapper>> initMatches(List<String> namedMatches,
            Collection<AbstractStatement> statements, Map<String, Node<StatementWrapper>> definers,
            Map<String, List<Node<StatementWrapper>>> referencers) {

        List<Node<StatementWrapper>> rootNodes = new ArrayList<>(4);

        collectVariables(namedMatches, null, statements, rootNodes, definers, referencers);

        Collection<Node<StatementWrapper>> ret = createGraph(rootNodes, definers, referencers);

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

    private static void collectVariables(List<String> namedMatches, Node<StatementWrapper> parent,
            Collection<AbstractStatement> statements, List<Node<StatementWrapper>> rootNodes,
            Map<String, Node<StatementWrapper>> definers,
            Map<String, List<Node<StatementWrapper>>> referencers) {

        for (AbstractStatement st : statements) {
            StatementWrapper match = new StatementWrapper(st,
                    st.isMatch() || namedMatches.contains(st.getDefinedVariable()));

            Node<StatementWrapper> node = new Node<>(match);

            node.setParent(parent);
            if (parent == null) {
                rootNodes.add(node);
            }

            if (st.getDefinedVariable() != null) {
                definers.put(st.getDefinedVariable(), node);
            }

            st.getReferencedVariables()
                    .forEach(v -> referencers.computeIfAbsent(v, __ -> new ArrayList<>()).add(node));

            collectVariables(namedMatches, node, st.getChildren(), rootNodes, definers, referencers);
        }
    }

    private static Collection<Node<StatementWrapper>> createGraph(List<Node<StatementWrapper>> rootNodes,
            Map<String, Node<StatementWrapper>> definers,
            Map<String, List<Node<StatementWrapper>>> referencers) {

        // establish the dependencies caused by the variable references
        definers.forEach((name, node) -> {
            referencers.getOrDefault(name, emptyList()).forEach(refNode -> {
                node.out().add(refNode);
                refNode.in().add(node);
            });
        });

        // and now just collect the whole graph into the resulting collection
        Set<Node<StatementWrapper>> ret = new HashSet<>();
        addAllRecursively(rootNodes, ret);

        return ret;
    }

    private static <T> void addAllRecursively(Collection<Node<T>> nodes, Collection<Node<T>> all) {
        for (Node<T> n : nodes) {
            all.add(n);
            addAllRecursively(n.getChildren(), all);
        }
    }
}
