package org.revapi.classif.util.execution;

import java.util.HashSet;
import java.util.Set;

public final class Node<T> implements Cloneable {
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

    public T getObject() {
        return object;
    }

    public SplitGroup<T> getSplitGroup() {
        return group;
    }

    public Set<Node<T>> in() {
        return in;
    }

    public Set<Node<T>> out() {
        return out;
    }

    @SuppressWarnings("unchecked")
    public Node<T> clone() {
        try {
            return (Node<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloneable is implemented by failed.");
        }
    }

    @Override
    public String toString() {
        return "Node{" +
                "object=" + object +
                ", i" + group.getSplits().indexOf(this)  +
                '}';
    }
}
