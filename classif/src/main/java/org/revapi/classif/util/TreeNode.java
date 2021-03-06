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
package org.revapi.classif.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A tree node with automatic management of parent-child relationship. This is an abstract base class to be extended
 * by a concrete type, {@link This}.
 *
 * @param <This> the type of the concrete subclass extending this base class
 */
public abstract class TreeNode<This extends TreeNode<This>> implements Cloneable {

    private This parent;
    private ParentPreservingSet children = new ParentPreservingSet();

    public This getParent() {
        return parent;
    }

    public void setParent(This parent) {
        this.parent = parent;
        if (parent != null) {
            parent._getChildren()._add(castThis());
        }
    }

    @SuppressWarnings("WeakerAccess")
    void _setParent(This parent) {
        this.parent = parent;
    }

    @SuppressWarnings("WeakerAccess")
    ParentPreservingSet _getChildren() {
        return children;
    }

    public Set<This> getChildren() {
        return children;
    }

    @SuppressWarnings("unchecked")
    @Override
    public This clone() {
        try {
            return (This) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloneable is implemented by failed.");
        }
    }

    @SuppressWarnings("unchecked")
    private This castThis() {
        return (This) this;
    }

    private class ParentPreservingSet implements Set<This>, Cloneable {
        private Set<This> actual = new HashSet<>();

        @Override
        public int size() {
            return actual.size();
        }

        @Override
        public boolean isEmpty() {
            return actual.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return actual.contains(o);
        }

        @Override
        public Iterator<This> iterator() {
            return new ParentPreservingIterator(actual.iterator());
        }

        @Override
        public Object[] toArray() {
            return actual.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return actual.toArray(a);
        }

        public void _add(This t) {
            actual.add(t);
        }

        @Override
        public boolean add(This t) {
            boolean ret = actual.add(t);
            if (ret) {
                t._setParent(castThis());
            }

            return ret;
        }

        @Override
        public boolean remove(Object o) {
            Iterator<This> it = this.iterator();
            while (it.hasNext()) {
                This e = it.next();
                if ((o == null && e == null) || (o != null && o.equals(e))) {
                    it.remove();
                    if (e != null) {
                        e._setParent(null);
                    }
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return actual.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends This> c) {
            for (This e : c) {
                add(e);
            }

            return !c.isEmpty();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean ret = false;

            for (Object o : c) {
                if (!contains(o)) {
                    ret = true;
                    remove(o);
                }
            }
            return ret;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean ret = false;
            for (Object o : c) {
                ret |= remove(o);
            }

            return ret;
        }

        @Override
        public void clear() {
            for (This e : this) {
                e.setParent(null);
            }

            actual.clear();
        }

        @Override
        public boolean equals(Object o) {
            return actual.equals(o);
        }

        @Override
        public int hashCode() {
            return actual.hashCode();
        }
        private class ParentPreservingIterator implements Iterator<This> {
            private final Iterator<This> it;
            This last;

            private ParentPreservingIterator(Iterator<This> it) {
                this.it = it;
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public This next() {
                last = it.next();
                return last;
            }

            @Override
            public void remove() {
                it.remove();
                if (last != null) {
                    last.setParent(null);
                }
            }
        }
    }
}
