package org.revapi.classif.util.unwind;

import java.util.ArrayList;
import java.util.List;

final class SplitGroup<T> {
    private final Node<T> original;
    private final List<Node<T>> splits = new ArrayList<>(4);

    SplitGroup(Node<T> original) {
        this.original = original;
        this.splits.add(original);
    }

    Node<T> getOriginal() {
        return original;
    }

    List<Node<T>> getSplits() {
        return splits;
    }

    boolean isUnsplit() {
        return splits.size() == 1 && splits.get(0) == original;
    }
}
