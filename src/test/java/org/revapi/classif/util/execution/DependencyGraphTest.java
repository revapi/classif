package org.revapi.classif.util.execution;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.statement.GenericStatement;
import org.revapi.classif.util.Nullable;

public class DependencyGraphTest {
    @Test
    void testAcyclicStatement() {
        DependencyGraph g = new DependencyGraph(emptyList(), asList(
                statement(null, "a", "b"),
                statement("a"),
                statement("b")));

        assertEquals(3, g.getAllNodes().size());

        assertTrue(g.getAllNodes().stream().anyMatch(n ->
                n.in().isEmpty() && "a".equals(n.getObject().definedVariable) && n.getObject().referencedVariables.isEmpty()
                        && n.out().size() == 1));

        assertTrue(g.getAllNodes().stream().anyMatch(n ->
                n.in().isEmpty() && "b".equals(n.getObject().definedVariable) && n.getObject().referencedVariables.isEmpty()
                        && n.out().size() == 1));

        assertTrue(g.getAllNodes().stream().anyMatch(n ->
                n.in().size() == 2 && n.getObject().definedVariable == null && n.getObject().referencedVariables.equals(asList("a", "b"))
                        && n.out().isEmpty()));
    }

    @Test
    void testCyclicStatement() {
        try {
            new DependencyGraph(emptyList(), asList(
                    statement("a", "b"),
                    statement("b", "c"),
                    statement("c", "a", "b")));

            Assertions.fail("Should not be able to create a cyclic graph");
        } catch (IllegalArgumentException e) {
            //good
        }
    }

    private static GenericStatement statement(@Nullable String definedVariable, String... referencedVariables) {
        return new GenericStatement(definedVariable, asList(referencedVariables),
                new AnnotationsMatch(emptyList()), new ModifiersMatch(emptyList()), true, false, null);
    }
}
