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

import java.util.HashSet;
import java.util.Set;

import org.revapi.classif.util.TreeNode;

/**
 * Dependency graph nodes form 2 structures actually. They form a tree observable using the {@link #getParent()}
 * and {@link #getChildren()} methods. Such tree represents the encapsulation of the nodes (e.g. when the
 * {@link DependencyGraph} is being constructed from the statements and variables, the tree structure represents the
 * nesting of the statements.
 *
 * <p>The second kind of structure is a DAG representing the dependencies between nodes formed by variable references.
 * This DAG is observable using the {@link #in()} and {@link #out()} methods.
 *
 * @param <T> the type of the data stored in the node. This class is also internally used in MatchingProgress and
 *           therefore is not bound to hold just the {@link MatchExecutionContext} which {@link DependencyGraph} uses.
 */
public final class Node<T> extends TreeNode<Node<T>> implements Cloneable {
    private final T object;
    private final Set<Node<T>> in = new HashSet<>(2);
    private final Set<Node<T>> out = new HashSet<>(2);

    public Node(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public Set<Node<T>> in() {
        return in;
    }

    public Set<Node<T>> out() {
        return out;
    }

    @Override
    public String toString() {
        if (getChildren().isEmpty()) {
            return object.toString() + ";";
        } else {
            StringBuilder bld = new StringBuilder(object.toString());
            bld.append("{\n");
            for (Node<T> c : getChildren()) {
                bld.append(c.toString());
                bld.append("\n");
            }
            bld.append("}");

            return bld.toString();
        }
    }
}
