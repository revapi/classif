package org.revapi.classif.util.execution;

import static java.util.Collections.emptySet;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class UnwindingGraph<T> {
    private final Collection<Node<T>> nodes;

    public UnwindingGraph(Collection<Node<T>> graph) {
        this.nodes = graph;
    }

    public Collection<Node<T>> unwind() {
        Set<Cycle<T>> cycles = new HashSet<>();

        detectCycles(cycles);

        Cycle<T> tmp = firstOrNull(cycles);
        if (tmp == null) {
            // cool, no cycles!
            return nodes;
        }

        Cycle<T> cyc = tmp;

        while (cyc != null) {
            if (cyc.size() == 1) {
                // a self loop
                splitSelfLoop(cyc.get(0));
            } else {
                // ok, we're looking at a cycle here. First we need to find a node in the cycle that has not been split yet
                // and split it...
                int splitIdx = -1;
                for (int i = 0; i < cyc.size(); ++i) {
                    if (cyc.get(i).getSplitGroup().isUnsplit()) {
                        splitIdx = i;
                        break;
                    }
                }

                splitNode(cyc.get(splitIdx), cyc);
            }

            // XXX is there a more efficient way to rebuild the list of the cycles after a single split?
            cycles.clear();
            detectCycles(cycles);
            cyc = firstOrNull(cycles);
        }

        nodes.addAll(nodes.stream().flatMap(n -> restoreOutCardinality(n).stream())
                .collect(Collectors.toList()));

        return nodes;
    }

    private void splitSelfLoop(Node<T> node) {
        Node<T> start = node.clone();
        Node<T> intermediary = node.clone();
        Node<T> end = node.clone();

        nodes.remove(node);
        nodes.add(start);
        nodes.add(intermediary);
        nodes.add(end);

        node.getSplitGroup().getSplits().remove(node);
        node.getSplitGroup().getSplits().add(start);
        node.getSplitGroup().getSplits().add(intermediary);
        node.getSplitGroup().getSplits().add(end);

        start.out().add(intermediary);
        intermediary.in().add(start);
        intermediary.out().add(end);
        end.in().add(intermediary);
    }

    private void splitNode(Node<T> node, List<Node<T>> cycle) {
        //split the node in two, sharing the same SplitGroup. The original node is removed.
        Node<T> inheritIncoming = node.clone();
        Node<T> inheritOutGoing = node.clone();

        nodes.add(inheritIncoming);
        nodes.add(inheritOutGoing);
        nodes.remove(node);

        node.getSplitGroup().getSplits().remove(node);
        node.getSplitGroup().getSplits().add(inheritIncoming);
        node.getSplitGroup().getSplits().add(inheritOutGoing);

        //one of the nodes inherits all the incoming edges from nodes in the cycle, the other node inherits
        //all the outgoing edges to the nodes in the cycle.
        //both nodes then also inherit incoming and outgoing edges to nodes that are not in the cycle.

        //Note that this disconnects the "node" from the graph, but leaves the knowledge about the original
        //ins and outs on the node itself. This is important later on when we need to restore the out-cardinality.
        for (Node<T> n : node.in()) {
            n.out().remove(node);
            n.out().add(inheritIncoming);
            inheritIncoming.in().add(n);
            if (!cycle.contains(n)) {
                n.out().add(inheritOutGoing);
                inheritOutGoing.in().add(n);
            }
        }

        for (Node<T> n : node.out()) {
            n.in().remove(node);
            n.in().add(inheritOutGoing);
            inheritOutGoing.out().add(n);
            if (!cycle.contains(n)) {
                n.in().add(inheritIncoming);
                inheritIncoming.out().add(n);
            }
        }
    }

    private Set<Node<T>> restoreOutCardinality(Node<T> node) {
        int newRequired = 0;
        Map<SplitGroup<T>, List<Node<T>>> outHistogram = new IdentityHashMap<>();
        for (Node<T> out : node.out()) {
            List<Node<T>> outSg = outHistogram.computeIfAbsent(out.getSplitGroup(), __ -> new ArrayList<>(4));
            outSg.add(out);
            if (outSg.size() > 1) {
                newRequired += 1;
            }
        }

        if (newRequired == 0) {
            return emptySet();
        }

        Set<Node<T>> ret = new HashSet<>(newRequired);

        //now see if for some splitgroup the original number of edges was lower than what we have now
        for (Map.Entry<SplitGroup<T>, List<Node<T>>> e : outHistogram.entrySet()) {
            List<Node<T>> outInSameSplitGroup = e.getValue();

            // now, our graph doesn't allow multiple edges between 2 nodes - the edges are just dependencies.
            // we can exploit that fact and just see what out edges point to the same node. Then for each such edge
            // we'll copy the current node.

            if (outInSameSplitGroup.size() <= 1) {
                continue;
            }

            for (int i = 1; i < outInSameSplitGroup.size(); ++i) {
                Node<T> copy = node.clone();
                copy.getSplitGroup().getSplits().add(copy);

                ret.add(copy);
                for (Node<T> in : node.in()) {
                    in.out().add(copy);
                    copy.in().add(in);
                }
                Node<T> in = outInSameSplitGroup.get(i);
                copy.out().add(in);
                in.in().remove(node);
                in.in().add(copy);
                node.out().remove(in);
            }
        }

        return ret;
    }

    private void detectCycles(Set<Cycle<T>> cycles) {
        nodes.forEach(n -> detectCycles(n, cycles, new ArrayList<>()));
    }

    private static <T> void detectCycles(Node<T> node, Set<Cycle<T>> cycles,
            List<Node<T>> currentTraversal) {

        int myIdx = currentTraversal.indexOf(node);
        if (myIdx >= 0) {
            if (myIdx == 0) {
                cycles.add(new Cycle<>(currentTraversal));
            }
            return;
        }

        currentTraversal.add(node);

        for (Node<T> child : node.out()) {
            detectCycles(child, cycles, currentTraversal);
        }

        currentTraversal.remove(currentTraversal.size() - 1);
    }

    private static <T> T firstOrNull(Iterable<T> set) {
        Iterator<T> it = set.iterator();
        return it.hasNext() ? it.next() : null;
    }

    private static final class Cycle<T> extends AbstractList<Node<T>> {
        private final ArrayList<Node<T>> nodes = new ArrayList<>(8);
        private final Map<Node<T>, Void> presence = new HashMap<>(8);

        Cycle(Collection<Node<T>> elements) {
            addAll(elements);
        }

        @Override
        public boolean add(Node<T> element) {
            if (!presence.containsKey(element)) {
                modCount++;
                presence.put(element, null);
                return nodes.add(element);
            } else {
                return false;
            }
        }

        @Override
        public int indexOf(Object o) {
            return nodes.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return nodes.lastIndexOf(o);
        }

        @Override
        public void clear() {
            modCount++;
            nodes.clear();
            presence.clear();
        }

        @Override
        public Node<T> get(int index) {
            return nodes.get(index);
        }

        @Override
        public int size() {
            return nodes.size();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Cycle)) {
                return false;
            }

            Cycle<?> cycle = (Cycle<?>) o;
            return Objects.equals(presence, cycle.presence);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(presence);
        }

        @Override
        public String toString() {
            return nodes.toString();
        }
    }
}
