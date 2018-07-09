/*
 * Copyright 2018 Lukas Krejci
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
package org.revapi.classif;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public enum TestResult {
    PASSED, NOT_PASSED, DEFERRED;

    public static TestResult fromBoolean(boolean value) {
        return value ? PASSED : NOT_PASSED;
    }

    public boolean toBoolean(boolean undecidedValue) {
        switch (this) {
            case PASSED:
                return true;
            case NOT_PASSED:
                return false;
            case DEFERRED:
                return undecidedValue;
            default:
                throw new IllegalStateException("Unhandled TestResult value: " + this);
        }
    }

    public TestResult decide(boolean undecidedValue) {
        return fromBoolean(toBoolean(undecidedValue));
    }

    public TestResult and(TestResult other) {
        switch (this) {
            case PASSED:
                return other;
            case NOT_PASSED:
                return this;
            case DEFERRED:
                return other == NOT_PASSED ? other : this;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

    public TestResult and(Supplier<TestResult> other) {
        switch (this) {
            case PASSED:
                return other.get();
            case NOT_PASSED:
                return this;
            case DEFERRED:
                TestResult res = other.get();
                return res == NOT_PASSED ? res : this;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

    public TestResult or(TestResult other) {
        switch (this) {
            case PASSED:
                return this;
            case NOT_PASSED:
                return other;
            case DEFERRED:
                return other == PASSED ? other : this;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

    public TestResult or(Supplier<TestResult> other) {
        switch (this) {
            case PASSED:
                return this;
            case NOT_PASSED:
                return other.get();
            case DEFERRED:
                TestResult res = other.get();
                return res == PASSED ? res : this;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

    public TestResult negate() {
        switch (this) {
            case PASSED:
                return NOT_PASSED;
            case NOT_PASSED:
                return PASSED;
            case DEFERRED:
                return DEFERRED;
        }

        throw new IllegalStateException("Unhandled TestResult: " + this);
    }

    @FunctionalInterface
    public interface Predicate<T> {
        TestResult test(T value);
    }

    @FunctionalInterface
    public interface BiPredicate<T, U> {
        TestResult test(T a, U b);
    }

    public static class TestableStream<T> implements Stream<T> {

        private final Stream<T> wrapped;

        private TestableStream(Stream<T> wrapped) {
            this.wrapped = wrapped;
        }

        public static <T> TestableStream<T> testable(Stream<T> stream) {
            return new TestableStream<>(stream);
        }

        public static <T> TestableStream<T> testable(Collection<T> collection) {
            return testable(collection.stream());
        }

        public TestResult testAny(Predicate<? super T> predicate) {
            return wrapped.reduce(NOT_PASSED, (res, next) -> res.or(() -> predicate.test(next)), TestResult::or);
        }

        public TestResult testAll(Predicate<? super T> predicate) {
            return wrapped.reduce(PASSED, (res, next) -> res.and(() -> predicate.test(next)), TestResult::and);
        }

        public TestResult testNone(Predicate<? super T> predicate) {
            return wrapped.reduce(PASSED, (res, next) -> res.and(() -> predicate.test(next)).negate(), TestResult::and);
        }

        // impl of the stream interface

        @Override
        public TestableStream<T> filter(java.util.function.Predicate<? super T> predicate) {
            return testable(wrapped.filter(predicate));
        }

        @Override
        public <R> TestableStream<R> map(Function<? super T, ? extends R> mapper) {
            return testable(wrapped.map(mapper));
        }

        @Override
        public IntStream mapToInt(ToIntFunction<? super T> mapper) {
            return wrapped.mapToInt(mapper);
        }

        @Override
        public LongStream mapToLong(ToLongFunction<? super T> mapper) {
            return wrapped.mapToLong(mapper);
        }

        @Override
        public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
            return wrapped.mapToDouble(mapper);
        }

        @Override
        public <R> TestableStream<R> flatMap(
                Function<? super T, ? extends Stream<? extends R>> mapper) {
            return testable(wrapped.flatMap(mapper));
        }

        @Override
        public IntStream flatMapToInt(
                Function<? super T, ? extends IntStream> mapper) {
            return wrapped.flatMapToInt(mapper);
        }

        @Override
        public LongStream flatMapToLong(
                Function<? super T, ? extends LongStream> mapper) {
            return wrapped.flatMapToLong(mapper);
        }

        @Override
        public DoubleStream flatMapToDouble(
                Function<? super T, ? extends DoubleStream> mapper) {
            return wrapped.flatMapToDouble(mapper);
        }

        @Override
        public TestableStream<T> distinct() {
            return testable(wrapped.distinct());
        }

        @Override
        public TestableStream<T> sorted() {
            return testable(wrapped.sorted());
        }

        @Override
        public TestableStream<T> sorted(Comparator<? super T> comparator) {
            return testable(wrapped.sorted(comparator));
        }

        @Override
        public TestableStream<T> peek(Consumer<? super T> action) {
            return testable(wrapped.peek(action));
        }

        @Override
        public TestableStream<T> limit(long maxSize) {
            return testable(wrapped.limit(maxSize));
        }

        @Override
        public TestableStream<T> skip(long n) {
            return testable(wrapped.skip(n));
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            wrapped.forEach(action);
        }

        @Override
        public void forEachOrdered(Consumer<? super T> action) {
            wrapped.forEachOrdered(action);
        }

        @Override
        public Object[] toArray() {
            return wrapped.toArray();
        }

        @Override
        public <A> A[] toArray(IntFunction<A[]> generator) {
            return wrapped.toArray(generator);
        }

        @Override
        public T reduce(T identity, BinaryOperator<T> accumulator) {
            return wrapped.reduce(identity, accumulator);
        }

        @Override
        public Optional<T> reduce(BinaryOperator<T> accumulator) {
            return wrapped.reduce(accumulator);
        }

        @Override
        public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator,
                BinaryOperator<U> combiner) {
            return wrapped.reduce(identity, accumulator, combiner);
        }

        @Override
        public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator,
                BiConsumer<R, R> combiner) {
            return wrapped.collect(supplier, accumulator, combiner);
        }

        @Override
        public <R, A> R collect(Collector<? super T, A, R> collector) {
            return wrapped.collect(collector);
        }

        @Override
        public Optional<T> min(Comparator<? super T> comparator) {
            return wrapped.min(comparator);
        }

        @Override
        public Optional<T> max(Comparator<? super T> comparator) {
            return wrapped.max(comparator);
        }

        @Override
        public long count() {
            return wrapped.count();
        }

        @Override
        public boolean anyMatch(java.util.function.Predicate<? super T> predicate) {
            return wrapped.anyMatch(predicate);
        }

        @Override
        public boolean allMatch(java.util.function.Predicate<? super T> predicate) {
            return wrapped.allMatch(predicate);
        }

        @Override
        public boolean noneMatch(java.util.function.Predicate<? super T> predicate) {
            return wrapped.noneMatch(predicate);
        }

        @Override
        public Optional<T> findFirst() {
            return wrapped.findFirst();
        }

        @Override
        public Optional<T> findAny() {
            return wrapped.findAny();
        }

        @Override
        public Iterator<T> iterator() {
            return wrapped.iterator();
        }

        @Override
        public Spliterator<T> spliterator() {
            return wrapped.spliterator();
        }

        @Override
        public boolean isParallel() {
            return wrapped.isParallel();
        }

        @Override
        public TestableStream<T> sequential() {
            return testable(wrapped.sequential());
        }

        @Override
        public TestableStream<T> parallel() {
            return testable(wrapped.parallel());
        }

        @Override
        public TestableStream<T> unordered() {
            return testable(wrapped.unordered());
        }

        @Override
        public TestableStream<T> onClose(Runnable closeHandler) {
            return testable(wrapped.onClose(closeHandler));
        }

        @Override
        public void close() {
            wrapped.close();
        }
    }
}
