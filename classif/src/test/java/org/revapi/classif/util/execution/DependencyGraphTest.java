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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.statement.AbstractStatement;
import org.revapi.classif.statement.GenericStatement;
import org.revapi.classif.util.Nullable;

public class DependencyGraphTest {
    @Test
    void testAcyclicStatement() {
        AbstractStatement top = statement(null, "a", "b");
        AbstractStatement a = statement("a");
        AbstractStatement b = statement("b");
        b.setParent(a);

        DependencyGraph g = new DependencyGraph(emptyList(), asList(top, a));

        assertEquals(3, g.getAllNodes().size());

        Node<MatchExecutionContext> topMatch = g.getAllNodes().stream()
                .filter(n -> n.getObject().definedVariable == null).findFirst().orElseThrow(AssertionError::new);

        Node<MatchExecutionContext> aMatch = g.getAllNodes().stream()
                .filter(n -> "a".equals(n.getObject().definedVariable)).findFirst().orElseThrow(AssertionError::new);

        Node<MatchExecutionContext> bMatch = g.getAllNodes().stream()
                .filter(n -> "b".equals(n.getObject().definedVariable)).findFirst().orElseThrow(AssertionError::new);

        assertEquals(asList("a", "b"), topMatch.getObject().referencedVariables);
        assertTrue(topMatch.out().isEmpty());
        assertEquals(2, topMatch.in().size());
        assertNull(topMatch.getParent());
        assertTrue(topMatch.getChildren().isEmpty());

        assertTrue(aMatch.getObject().referencedVariables.isEmpty());
        assertEquals(1, aMatch.out().size());
        assertTrue(aMatch.in().isEmpty());
        assertNull(aMatch.getParent());
        assertEquals(1, aMatch.getChildren().size());

        assertTrue(bMatch.getObject().referencedVariables.isEmpty());
        assertEquals(1, bMatch.out().size());
        assertTrue(bMatch.in().isEmpty());
        assertEquals(aMatch, bMatch.getParent());
        assertTrue(bMatch.getChildren().isEmpty());
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
