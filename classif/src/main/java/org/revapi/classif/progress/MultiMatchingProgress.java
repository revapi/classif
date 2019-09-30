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
package org.revapi.classif.progress;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import static org.revapi.classif.TestResult.DEFERRED;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;
import static org.revapi.classif.util.LogUtil.traceParams;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.EntryMessage;
import org.revapi.classif.StructuralMatcher;
import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.classif.util.Nullable;
import org.revapi.classif.util.execution.Node;

/**
 * Graph of statements:
 *
 * <pre>{@code
 * type ^%y=* extends %x {                            <1>
 *   <init>();                                        <2>
 * }
 *
 * class %x=* directly extends java.lang.Object {}    <3>
 *
 * class * extends %y {}                              <4>
 * }</pre>
 * <p>
 * Can be visually represented as:
 *
 * <pre>{@code
 * +---+     +---+
 * | 1 |---->| 3 |
 * +---+     +---+
 *   |         ^
 *   v         |
 * +---+     +---+
 * | 2 |     | 4 |
 * +---+     +---+
 * }</pre>
 * <p>
 * Matching algorithm at a high level:
 * <ol>
 * <li> Elements enter the graph in a tree like manner. The matching algorithm instructs the caller how to proceed with
 *      the hierarchical traversal of the elements.
 * <li> An element matches the statement graph if at least 1 returning statement (number 1 in the example) matches it.
 * <li> A statement matches an element if the element satisfies the following conditions:
 *    <ol>
 *    <li> all children of the statement match at least 1 child of the element each
 *    <li> each referenced statement matches the type that is referenced by the element on the appropriate position
 *    <li> all statements that reference the current statement match at least 1 element
 *    <li> the statement matches the element disregarding the references, referents and children
 *    </ol>
 * </ol>
 * <p>
 * Example:
 *
 * <pre>{@code
 * class A {}
 *
 * class B extends A {}
 *
 * class C extends B {}
 * }</pre>
 * <p>
 * {@code class A} doesn't match the example graph, because the returning statement ({@code <1>}) doesn't match it
 * ({@code class A} doesn't extends another class that would extend {@code Object}, but rather extends
 * {@code Object} directly itself).
 * <p>
 * {@code class B} matches the example graph because of these reasons:
 * <ol>
 * <li> It extends {@code class A} which directly extends {@code Object} - e.g. {@code class A} satisfies the {@code %x}
 * variable in {@code <1>}.
 * <li> It has a no-arg constructor and therefore the no-arg constructor method satisfies statement {@code <2>}.
 * <li> {@code class C} satisfies statement {@code <4>} which references {@code <1>}, because it extends {@code class B}
 * which is evaluated by {@code <1>}.
 * </ol>
 * Importantly, if we didn't pass {@code class C} to the matcher, {@code class B} WOULDN'T match, because {@code <4>}
 * would not be satisfied by any element seen by the matcher.
 */
final class MultiMatchingProgress<M> extends MatchingProgress<M> {
    private static final Logger LOG = LogManager.getLogger(MultiMatchingProgress.class);

    private final List<Node<StatementMatch<M>>> roots;
    private final Deque<WalkContext<M>> statementStack = new ArrayDeque<>();
    private final StructuralMatcher.Configuration config;
    private final Set<WalkContext<M>> undecided;
    private final List<Node<StatementMatch<M>>> returningStatements;
    private final Map<String, Node<StatementMatch<M>>> definingStatements;

    MultiMatchingProgress(StructuralMatcher.Configuration configuration,
            List<Node<StatementMatch<M>>> statements) {
        config = configuration;
        roots = new ArrayList<>();
        returningStatements = new ArrayList<>();
        definingStatements = new HashMap<>();

        statements.forEach(s -> {
            if (s.getParent() == null) {
                roots.add(s);
            }

            if (s.getObject().getContext().isReturn()) {
                returningStatements.add(s);
            }

            String var = s.getObject().getContext().getDefinedVariable();
            if (var != null) {
                definingStatements.put(var, s);
            }
        });
        undecided = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    @Override
    public WalkInstruction start(M model) {
        EntryMessage methodTrace = LOG.traceEntry(traceParams(LOG, "this", this, "model", model));

        WalkContext<M> parentCtx = statementStack.isEmpty() ? null : requireNonNull(statementStack.peek());
        Collection<Node<StatementMatch<M>>> currentStatements = parentCtx == null ? roots : parentCtx.nextStatements;
        Collection<Node<StatementMatch<M>>> nextStatements = new ArrayList<>();

        boolean mustHaveChildren = true;
        TestResult res = NOT_PASSED;
        for (Node<StatementMatch<M>> sm : currentStatements) {
            TestResult sr = sm.getObject().independentTest(model);

            if (sm.getObject().getContext().isReturn()) {
                res = res.or(sr);
            }

            if (!(sm.out().isEmpty() && sm.in().isEmpty())) {
                // even though we're basically re-setting the result here to DEFERRED, we need to let all the tests
                // go through so that we're collecting the match candidates at the individual statements
                res = DEFERRED;
            }

            nextStatements.addAll(sm.getChildren());
            mustHaveChildren = mustHaveChildren && !sm.getChildren().isEmpty();
        }

        if (!config.isStrictHierarchy()) {
            nextStatements.addAll(roots);
        }

        boolean descend = !config.isStrictHierarchy() || !nextStatements.isEmpty();

        statementStack.push(new WalkContext<>(parentCtx, model, res, mustHaveChildren, nextStatements));

        return LOG.traceExit(methodTrace, WalkInstruction.of(descend, res));
    }

    @Override
    public TestResult finish(M model) {
        EntryMessage methodTrace = LOG.traceEntry(traceParams(LOG, "this", this, "model", model));

        if (statementStack.isEmpty()) {
            throw LOG.traceExit(methodTrace, new IllegalStateException("Unbalanced start/finish calls."));
        }

        WalkContext<M> ctx = statementStack.pop();

        if (ctx.model != model) {
            throw LOG.traceExit(methodTrace, new IllegalStateException("Unbalanced start/finish calls."));
        }

        if (ctx.mustHaveChildren && !ctx.childrenEncountered) {
            ctx.finishResult = NOT_PASSED;
        }

        if (ctx.finishResult == NOT_PASSED) {
            LOG.trace("start of model {} didn't pass, so bailing out quickly.", model);
            return LOG.traceExit(methodTrace, NOT_PASSED);
        }

        if (ctx.parent != null) {
            ctx.parent.finishResult = ctx.parent.finishResult.and(ctx.finishResult);
            ctx.parent.childrenEncountered = true;
        }

        if (ctx.finishResult == DEFERRED) {
            undecided.add(ctx);
        }

        return ctx.finishResult;
    }

    @Override
    public Map<M, TestResult> finish() {
        EntryMessage methodTrace = LOG.traceEntry(traceParams(LOG, "this", this));

        // 1. Find all the statements defining variables
        // 2. Build a context for each permutation of candidate models for those statements
        // 3. for each undecided model, for each returning statement, for each context, compute the union of the results
        // for the statement with the model and the context

        if (undecided.isEmpty()) {
            return LOG.traceExit(methodTrace, emptyMap());
        }

        List<String> vars = new ArrayList<>(definingStatements.keySet());
        List<Collection<M>> candidates = definingStatements.values().stream().map(n -> n.getObject().getCandidates())
                .map(c -> c.isEmpty() ? Collections.<M>singleton(null) : c)
                .collect(toList());

        Map<M, TestResult> ret = undecided.stream().map(wc -> wc.model).collect(Collectors.toMap(identity(), model -> {
            for (Node<StatementMatch<M>> st : returningStatements) {
                Iterator<List<M>> combinations = combinations(candidates);
                while (combinations.hasNext()) {
                    List<M> combination = combinations.next();
                    Map<String, M> binding = new HashMap<>(vars.size());
                    for (int i = 0; i < vars.size(); ++i) {
                        binding.put(vars.get(i), combination.get(i));
                    }

                    StatementMatch<M> match = st.getObject();
                    TestResult combinationResult = testBinding(st, model, binding, new IdentityHashMap<>());
                    if (combinationResult == PASSED) {
                        return PASSED;
                    }
                }
            }

            return NOT_PASSED;
        }));

        return LOG.traceExit(methodTrace, ret);
    }

    private TestResult testBinding(Node<StatementMatch<M>> statementNode, M model, Map<String, M> binding,
            Map<Node<StatementMatch<M>>, Map<M, TestResult>> cache) {

        EntryMessage methodTrace = LOG.traceEntry(traceParams(LOG, "this", this, "statementNode", statementNode, "model", model, "binding", binding));

        TestResult cached = cache.computeIfAbsent(statementNode, __ -> new HashMap<>()).get(model);
        if (cached != null) {
            LOG.trace("Found cached result {} for {}", cached, statementNode);
            return LOG.traceExit(methodTrace, cached);
        } else {
            if (cache.get(statementNode).containsKey(model)) {
                // the evaluation is in progress, yet we arrived here again... pretending a passing result basically
                // cancels out the effect of this statement on the result
                LOG.trace("Evaluation loop detected on {}", statementNode);
                return LOG.traceExit(methodTrace, PASSED);
            }
        }

        cache.get(statementNode).put(model, null);

        StatementMatch<M> match = statementNode.getObject();

        TestResult ret = match.test(model, match.getContext().require(binding).getMatchContext())
                .and(() -> {
                    // match children
                    EntryMessage trace = LOG.traceEntry("Matching children");

                    if (statementNode.getChildren().isEmpty()) {
                        LOG.trace("No children on statement {}", statementNode);
                        return LOG.traceExit(trace, PASSED);
                    }

                    for (Node<StatementMatch<M>> child : statementNode.getChildren()) {
                        StatementMatch<M> childSt = child.getObject();
                        boolean somePassed = false;
                        for (M candidate : childSt.getCandidates()) {
                            MatchContext<M> ctx = childSt.getContext().require(binding).getMatchContext();
                            Element parent = ctx.getModelInspector().toElement(candidate).getEnclosingElement();
                            if (parent == null || !model.equals(ctx.getModelInspector().fromElement(parent))) {
                                continue;
                            }
                            if (testBinding(child, candidate, binding, cache) == PASSED) {
                                somePassed = true;
                                break;
                            }
                        }

                        if (!somePassed) {
                            LOG.trace("No candidate on child {} passes with binding {}", child, binding);
                            return LOG.traceExit(trace, NOT_PASSED);
                        }
                    }

                    return LOG.traceExit(trace, PASSED);
                })
                .and(() -> {
                    // match dependencies
                    EntryMessage trace = LOG.traceEntry("Matching dependencies");

                    if (statementNode.in().isEmpty()) {
                        LOG.trace("No dependencies found on {}", statementNode);
                        return LOG.traceExit(trace, PASSED);
                    }

                    TestResult result = requireDependencies(model, statementNode, binding, cache);

                    return LOG.traceExit(trace, result);
                })
                .and(() -> {
                    // match dependents
                    EntryMessage trace = LOG.traceEntry("Matching dependents");

                    if (statementNode.out().isEmpty()) {
                        LOG.trace("No dependents on statement {}", statementNode);
                        return LOG.traceExit(trace, PASSED);
                    }

                    String var = match.getContext().getDefinedVariable();
                    if (model != binding.get(var)) {
                        LOG.trace("Current model {} is not bound as {} on the current statement {}." +
                                " Dependents cannot pass.", model, var, statementNode);
                        return LOG.traceExit(trace, NOT_PASSED);
                    }

                    TestResult result = PASSED;

                    for (Node<StatementMatch<M>> dep : statementNode.out()) {
                        StatementMatch<M> depSt = dep.getObject();
                        boolean someCandidatePasses = false;
                        for (M depCandidate : depSt.getCandidates()) {
                            if (testBinding(dep, depCandidate, binding, cache) == PASSED) {
                                someCandidatePasses = true;
                                break;
                            }
                        }

                        if (!someCandidatePasses) {
                            LOG.trace("No candidate match of {} matches with binding {}.", dep, binding);
                            result = NOT_PASSED;
                            break;
                        }
                    }

                    return LOG.traceExit(trace, result);
                });

        cache.get(statementNode).put(model, ret);

        return LOG.traceExit(methodTrace, ret);
    }

    private TestResult requireDependencies(M model, Node<StatementMatch<M>> statementNode, Map<String, M> binding,
            Map<Node<StatementMatch<M>>, Map<M, TestResult>> cache) {
        // potential optimization here is to as the statement whether it needs to process all the dependencies or
        // if we can somehow short-circuit here - either if the statement requires all deps to pass and we find
        // a non-matching or if the statement requires at least one dep to pass and we find such.
        // This requires some complex logic in the matches though because we need to basically reconstruct the
        Map<String, TestResult> results = new HashMap<>();
        for (Node<StatementMatch<M>> dep : statementNode.in()) {
            String var = dep.getObject().getContext().getDefinedVariable();
            if (dep.getObject().getCandidates().contains(binding.get(var))) {
                results.put(var, testBinding(dep, binding.get(var), binding, cache));
            } else {
                results.put(var, NOT_PASSED);
            }
        }

        return statementNode.getObject()
                .test(model, statementNode.getObject().getContext().withResults(results).getMatchContext());
    }

    @Override
    public void reset() {
        statementStack.clear();
        undecided.clear();
        roots.forEach(this::resetStatement);
    }

    private void resetStatement(Node<StatementMatch<M>> st) {
        st.getObject().reset();
        for (Node<StatementMatch<M>> child : st.getChildren()) {
            resetStatement(child);
        }
    }

    private static class WalkContext<M> {
        final @Nullable WalkContext<M> parent;
        final M model;
        final TestResult startResult;
        TestResult finishResult;
        final boolean mustHaveChildren;
        boolean childrenEncountered;
        final Collection<Node<StatementMatch<M>>> nextStatements;

        private WalkContext(WalkContext<M> parent, M model, TestResult startResult, boolean shouldHaveChildren,
                Collection<Node<StatementMatch<M>>> nextStatements) {
            this.parent = parent;
            this.model = model;
            this.startResult = startResult;
            this.nextStatements = nextStatements;
            this.mustHaveChildren = shouldHaveChildren;
            this.finishResult = startResult;
        }
    }

    private <T> Iterator<List<T>> combinations(Collection<? extends Collection<T>> source) {
        return new Iterator<List<T>>() {
            final List<Collection<T>> sources = new ArrayList<>(source);
            final List<Iterator<T>> current = source.stream().map(Collection::iterator).collect(toList());
            List<T> last = null;

            @Override
            public boolean hasNext() {
                for (Iterator<?> it : current) {
                    if (it.hasNext()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public List<T> next() {
                if (last == null) {
                    return first();
                }

                for (int i = 0; i < last.size(); ++i) {
                    Iterator<T> it = current.get(i);
                    if (it.hasNext()) {
                        last.set(i, it.next());
                        return last;
                    } else {
                        it = sources.get(i).iterator();
                        current.set(i, it);
                        last.set(i, it.next());
                    }
                }

                throw new NoSuchElementException();
            }

            private List<T> first() {
                if (last == null) {
                    last = current.stream().map(Iterator::next).collect(toCollection(ArrayList::new));
                }
                return last;
            }
        };
    }
}
