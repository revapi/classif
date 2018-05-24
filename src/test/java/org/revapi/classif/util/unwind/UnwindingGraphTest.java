package org.revapi.classif.util.unwind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UnwindingGraphTest {

    @Test
    void testAcyclic() {
        Node<Void> n1 = new Node<>(null);
        Node<Void> n2 = new Node<>(null);
        Node<Void> n3 = new Node<>(null);

        n1.out().add(n2);
        n2.in().add(n1);
        n2.out().add(n3);
        n3.in().add(n2);

        List<Node<Void>> nodes = new ArrayList<>();
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);

        UnwindingGraph<Void> g = new UnwindingGraph<>(nodes);

        List<Node<Void>> newNodes = g.unwind(n -> null);

        assertEquals(nodes, newNodes);
    }

    @Test
    void testCyclic() {
        Node<Integer> n1 = new Node<>(1);
        Node<Integer> n2 = new Node<>(2);
        Node<Integer> n3 = new Node<>(3);

        n1.out().add(n2);
        n1.in().add(n3);
        n2.in().add(n1);
        n2.in().add(n3);
        n2.out().add(n3);
        n3.in().add(n2);
        n3.out().add(n2);
        n3.out().add(n1);

        List<Node<Integer>> nodes = new ArrayList<>();
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);

        UnwindingGraph<Integer> g = new UnwindingGraph<>(new ArrayList<>(nodes));

        List<Node<Integer>> newNodes = g.unwind(Node::getObject);
        Set<SplitGroup<Integer>> groups = bySplitGroups(newNodes);

        assertEquals(3, groups.size());

        SplitGroup<Integer> ones = firstWithObject(1, groups);
        SplitGroup<Integer> twos = firstWithObject(2, groups);
        SplitGroup<Integer> threes = firstWithObject(3, groups);

        assertEquals(3, ones.getSplits().size());
        assertEquals(2, twos.getSplits().size());
        assertEquals(1, threes.getSplits().size());

        for (Node<Integer> n : ones.getSplits()) {
            assertTrue(n.out().size() <= 1);
        }

        for (Node<Integer> n : twos.getSplits()) {
            assertTrue(n.out().size() <= 1);
        }
    }

    @Test
    void testSelfCycle() {
        Node<Integer> n1 = new Node<>(1);

        n1.out().add(n1);
        n1.in().add(n1);

        List<Node<Integer>> nodes = new ArrayList<>();
        nodes.add(n1);

        UnwindingGraph<Integer> g = new UnwindingGraph<>(new ArrayList<>(nodes));

        List<Node<Integer>> newNodes = g.unwind(Node::getObject);
        Set<SplitGroup<Integer>> groups = bySplitGroups(newNodes);

        assertEquals(1, groups.size());

        SplitGroup<Integer> ones = firstWithObject(1, groups);

        assertEquals(3, ones.getSplits().size());
    }

    private static <T> Set<SplitGroup<T>> bySplitGroups(Collection<Node<T>> nodes) {
        Set<SplitGroup<T>> ret = new HashSet<>();
        nodes.forEach(n -> ret.add(n.getSplitGroup()));
        return ret;
    }

    private static <T> SplitGroup<T> firstWithObject(T object, Set<SplitGroup<T>> groups) {
        return groups.stream().filter(g -> Objects.equals(object, g.getOriginal().getObject())).findFirst()
                .orElse(null);
    }
}
