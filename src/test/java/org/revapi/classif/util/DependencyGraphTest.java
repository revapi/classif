package org.revapi.classif.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.statement.GenericStatement;

public class DependencyGraphTest {
    @Test
    void testAcyclicStatement() {
        DependencyGraph g = new DependencyGraph(emptyList(), asList(
                statement(null, "a", "b"),
                statement("a"),
                statement("b")));

        assertEquals(3, g.getAllNodes().size());

        assertTrue(g.getAllNodes().stream().anyMatch(n ->
                n.in.isEmpty() && "a".equals(n.node.definedVariable) && n.node.referencedVariables.isEmpty()
                        && n.out.size() == 1));

        assertTrue(g.getAllNodes().stream().anyMatch(n ->
                n.in.isEmpty() && "b".equals(n.node.definedVariable) && n.node.referencedVariables.isEmpty()
                        && n.out.size() == 1));

        assertTrue(g.getAllNodes().stream().anyMatch(n ->
                n.in.size() == 2 && n.node.definedVariable == null && n.node.referencedVariables.equals(asList("a", "b"))
                        && n.out.isEmpty()));
    }

    @Test
    void testCyclicStatement() {
        DependencyGraph g = new DependencyGraph(emptyList(), asList(
                statement("a", "b"),
                statement("b", "c"),
                statement("c", "a", "b")));

        assertEquals(3, g.getAllNodes().size());

        assertTrue(g.getAllNodes().stream().anyMatch(n ->
                n.in.size() == 1 && "a".equals(n.node.definedVariable) && n.node.referencedVariables.equals(singletonList("b"))
                        && n.out.size() == 1));

        assertTrue(g.getAllNodes().stream().anyMatch(n ->
                n.in.size() == 1 && "b".equals(n.node.definedVariable) && n.node.referencedVariables.equals(singletonList("c"))
                        && n.out.size() == 2));

        assertTrue(g.getAllNodes().stream().anyMatch(n ->
                n.in.size() == 2 && "c".equals(n.node.definedVariable) && n.node.referencedVariables.equals(asList("a", "b"))
                        && n.out.size() == 1));
    }

    private static GenericStatement statement(@Nullable String definedVariable, String... referencedVariables) {
        return new GenericStatement(definedVariable, asList(referencedVariables),
                new AnnotationsMatch(emptyList()), new ModifiersMatch(emptyList()), true, false, null);
    }
}
