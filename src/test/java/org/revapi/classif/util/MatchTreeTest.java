package org.revapi.classif.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.statement.GenericStatement;

public class MatchTreeTest {

    @Test
    void testAcyclicUnwind() {
        DependencyGraph g = new DependencyGraph(emptyList(), asList(
                statement(null, "a", "b"),
                statement("a"),
                statement("b")));

        ProcessedMatch matcher = MatchTree.unwind(g);

        assertEquals(2, matcher.getChildren().size());
    }

    @Test
    void testCyclicUnwind() {

    }

    private static GenericStatement statement(@Nullable String definedVariable, String... referencedVariables) {
        return new GenericStatement(definedVariable, asList(referencedVariables),
                new AnnotationsMatch(emptyList()), new ModifiersMatch(emptyList()), true, false, null);
    }
}
