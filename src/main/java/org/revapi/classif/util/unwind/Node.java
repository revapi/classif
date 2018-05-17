package org.revapi.classif.util.unwind;

import java.util.HashSet;
import java.util.Set;

final class Node<T> {
    private final T object;
    private final Set<Node<T>> in = new HashSet<>(4);
    private final Set<Node<T>> out = new HashSet<>(4);
    private final SplitGroup<T> group;

    public Node(T object) {
        this.object = object;
        this.group = new SplitGroup<>(this);
    }

    public Node(T object, SplitGroup<T> group) {
        this.object = object;
        this.group = group;
    }

    T getObject() {
        return object;
    }

    SplitGroup<T> getSplitGroup() {
        return group;
    }

    Set<Node<T>> in() {
        return in;
    }

    Set<Node<T>> out() {
        return out;
    }

    @Override
    public String toString() {
        return "Node{" +
                "object=" + object +
                ", i" + group.getSplits().indexOf(this)  +
                '}';
    }
}
