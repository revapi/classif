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

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import static org.revapi.classif.TestResult.DEFERRED;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.util.execution.DependencyGraph;
import org.revapi.classif.util.execution.MatchExecutionContext;
import org.revapi.classif.util.execution.Node;

/**
 * The progress of the matching of the {@link StructuralMatcher} that created this instance.
 * <p>
 * The {@link #start(Object)} and {@link #finish(Object)} methods are used to instruct the caller on how to execute
 * the depth-first walk of the assumed tree structure of the model elements.
 *
 * @param <M> the type of the representation of the java model elements used by the caller
 */
public final class MatchingProgress<M> {
    private static final ModelMatch MATCH_ANY = new ModelMatch() {
        @Override
        protected <MM> TestResult defaultElementTest(MM model, MatchContext<MM> ctx) {
            return PASSED;
        }
    };

    private final List<Node<Step<M>>> allSteps;
    private final List<Node<Step<M>>> rootMatches;
    private final Deque<ProgressContext<M>> progressStack = new ArrayDeque<>();
    private final Set<M> deferredModels = new HashSet<>();
    private final StructuralMatcher.Configuration configuration;

    MatchingProgress(StructuralMatcher.Configuration configuration, DependencyGraph matchGraph,
            ModelInspector<M> inspector) {
        this.configuration = configuration;

        IdentityHashMap<Node<MatchExecutionContext>, Node<Step<M>>> cache =
                new IdentityHashMap<>(matchGraph.getAllNodes().size());

        allSteps = matchGraph.getAllNodes().stream()
                .map(n -> convert(n, inspector, cache))
                .collect(toList());

        // not too efficient way of figuring out whether some children return or use variables.
        // This is needed in #start().
        for (Node<Step<M>> n : allSteps) {
            boolean isReturn = n.getObject().executionContext.isReturn;
            boolean usesVariables = n.getObject().executionContext.definedVariable != null
                    || !n.getObject().executionContext.referencedVariables.isEmpty();

            n.getObject().thisOrChildrenReturn |= isReturn;
            n.getObject().thisOrChildrenUseVariables |= usesVariables;

            if (isReturn || usesVariables) {
                Node<Step<M>> parent = n.getParent();
                while (parent != null) {
                    boolean parentReturns = isReturn || parent.getObject().executionContext.isReturn;
                    boolean parentUsesVariables = usesVariables
                            || parent.getObject().executionContext.definedVariable != null
                            || !parent.getObject().executionContext.referencedVariables.isEmpty();

                    parent.getObject().thisOrChildrenReturn |= parentReturns;
                    parent.getObject().thisOrChildrenUseVariables |= parentUsesVariables;
                    parent = parent.getParent();
                }
            }
        }

        rootMatches = allSteps.stream().filter(n -> n.getParent() == null).collect(toList());
    }

    private static <M> Node<Step<M>> convert(Node<MatchExecutionContext> n, ModelInspector<M> inspector,
            Map<Node<MatchExecutionContext>, Node<Step<M>>> cache) {

        Node<Step<M>> ret = cache.get(n);

        if (ret != null) {
            return ret;
        }

        Map<String, ModelMatch> situationalMatches = new HashMap<>(n.getObject().referencedVariables.size());
        Map<String, ModelMatch> trivialMatches = new HashMap<>(situationalMatches.size());

        for (String v : n.getObject().referencedVariables) {
            ModelMatch variableMatch = null;
            for (Node<MatchExecutionContext> o : n.in()) {
                if (v.equals(o.getObject().definedVariable)) {
                    variableMatch = o.getObject().match;
                }
            }

            if (variableMatch == null) {
                throw new IllegalStateException("Invalid dependency graph. Could not find a node defining variable "
                        + v);
            }

            trivialMatches.put(v, MATCH_ANY);
            situationalMatches.put(v, variableMatch);
        }

        ret = new Node<>(new Step<>(n.getObject(), new MatchContext<>(inspector, situationalMatches),
                new MatchContext<>(inspector, trivialMatches)));

        cache.put(n, ret);

        for (Node<MatchExecutionContext> in : n.in()) {
            ret.in().add(convert(in, inspector, cache));
        }

        for (Node<MatchExecutionContext> out : n.out()) {
            ret.out().add(convert(out, inspector, cache));
        }

        for (Node<MatchExecutionContext> c : n.getChildren()) {
            ret.getChildren().add(convert(c, inspector, cache));
        }

        return ret;
    }

    /**
     * Starts a tree-walk of the provided java element. The caller is expected to continue the walk of the model
     * elements in the depth-first search manner. The returned instruction tells the caller the result of the test
     * of the provided model (at this stage) and also whether it is necessary to continue the walk into the children.
     *
     * <p>The caller does not have to obey that and can descend into the children even if the instruction tells
     * otherwise provided it still conforms to the contract of using the depth-first-search for the visits and calling
     * {@link #start(Object)} and {@link #finish(Object)} at appropriate times.
     *
     * <p>On the other hand, if the caller doesn't descend into the children even if the instruction said to do that,
     * the overall results of the tests might be wrong, because the children might have contained information needed
     * to determine the matches of other elements.
     *
     * @param model the model of the element
     * @return the result of the test of the model against the structural matcher and the suggestion on how to proceed
     * with the tree walk.
     */
    public WalkInstruction start(M model) {
        // fast track if we have just a single test to make
        if (allSteps.size() == 1) {
            Step<M> s = allSteps.iterator().next().getObject();
            TestResult result = s.executionContext.match.test(model, s.blueprintMatchContext);
            if (result.toBoolean(false)) {
                // use this as a kind of cache in the simple case
                s.independentlyMatchingModels.put(model, null);
            }

            return WalkInstruction.of(!configuration.isStrictHierarchy(), result);
        }

        // make sure parent has access to the children
        ProgressContext<M> parentProgress = progressStack.peek();
        ProgressContext<M> progressContext = new ProgressContext<>(model);

        // ==== Processing the step dependencies ====

        // for each dependency provider of the current potentials (i.e. its in() nodes), replace the variable it
        // provides with each of the independent matches it had so far and check what current potentials match with
        // the variable replaced as such. Do this recursively.
        potentialMatchesWithDependencies(model);

        TestResult res = forEachPotential(n -> {
            Step<M> step = n.getObject();
            if (step.executionContext.isReturn) {
                TestResult tr = step.resolutionCache.getOrDefault(model, DEFERRED);
                if (tr == PASSED && !n.getChildren().isEmpty()) {
                    // we're starting the descend into the model, so it really cannot pass without the children passing,
                    // too. We won't know that until finish(model) though.
                    tr = DEFERRED;
                }
                return tr;
            } else {
                return NOT_PASSED;
            }
        }, TestResult::or).orElseThrow(() -> new IllegalArgumentException("This should never happen." +
                " forEachPotential() succeeded to run with empty set of potentials."));

        // ==== Inform the parent about the outcome of this testing ====

        // Note that we MUST NOT use the result that we're returning to the user here. The user is interested in the
        // result of the returning matches whereas here, we want to update the parent on the result of matching of
        // the current model regardless of whether it matches some returning matches or not.

        TestResult rawResult = forEachPotential(n ->
                n.getObject().resolutionCache.getOrDefault(model, NOT_PASSED), TestResult::or)
                .orElseThrow(() -> new IllegalArgumentException("This should never happen." +
                        " forEachPotential() succeeded to run with empty set of potentials."));

        if (parentProgress != null) {
            parentProgress.childrenEncountered = true;
            parentProgress.childrenResolution = parentProgress.childrenResolution.and(rawResult);
        }

        // ==== Prepare for evaluating the children ====

        // We're starting a new node, so for all matching potential matches (including the already active "up"), we
        // evaluate and determine the new set of potential matches for the children

        progressContext.potentialChildMatches = determineNextPotentialMatchesFromHierarchy(model);
        progressContext.resolution = res;
        progressStack.push(progressContext);

        if (res == DEFERRED) {
            deferredModels.add(model);
        }

        boolean descend = !configuration.isStrictHierarchy() || !progressContext.potentialChildMatches.isEmpty();

        return WalkInstruction.of(descend, res);
    }

    /**
     * Called when the depth-first walk of the provided model element finished. I.e. this method is called after
     * all children are also visited after the {@link #start(Object)} method on this element has been called.
     *
     * <p>Note that the result can be different from the result obtained from the {@link #start(Object)} method because
     * it can be influenced by the executed tests on the children. It can still be {@link TestResult#DEFERRED} though
     * if the structural match uses a variable that has not yet been successfully evaluated during the tree walk.
     *
     * @param model the model of the checked element
     * @return the result of the test after all children have been visited
     */
    public TestResult finish(M model) {
        if (allSteps.size() == 1) {
            Step<M> s = allSteps.iterator().next().getObject();
            boolean matched = s.independentlyMatchingModels.containsKey(model);
            s.independentlyMatchingModels.remove(model);
            return TestResult.fromBoolean(matched);
        }

        ProgressContext<M> progressContext = progressStack.peek();

        if (progressContext == null || progressContext.model != model) {
            throw new IllegalArgumentException("Unbalanced start/finish call.");
        } else {
            progressStack.pop();
        }

        // get the result obtained in the #start()
        TestResult ret = progressContext.resolution;

        if (ret == DEFERRED) {
            ret = progressContext.potentialChildMatches.isEmpty()
                    ? PASSED
                    : TestResult.fromBoolean(progressContext.childrenEncountered);

            ret = ret.and(progressContext.childrenResolution)
                    // and at least one of the current potentials is a return and it passed.
                    .and(() ->
                            forEachPotential(n -> {
                                Step<M> step = n.getObject();
                                if (step.executionContext.isReturn) {
                                    return step.resolutionCache.getOrDefault(model, DEFERRED);
                                } else {
                                    return DEFERRED;
                                }
                            }, TestResult::or).orElse(DEFERRED));

            if (ret != DEFERRED) {
                deferredModels.remove(model);
            }
        }

        //make sure to update the parents notion of the results
        if (!progressStack.isEmpty()) {
            ProgressContext parentContext = requireNonNull(progressStack.peek());
            // as in #start() we must not use the result returned to the user here, because we're interested in the
            // "raw" passing or not passing of the model with the matches. The user on the other hand is only interested
            // in matching of the returning matches.
            parentContext.childrenResolution = parentContext.childrenResolution.and(() -> forEachPotential(n ->
                    n.getObject().resolutionCache.getOrDefault(model, NOT_PASSED), TestResult::or)
                    .orElseThrow(() -> new IllegalArgumentException("This should never happen." +
                            " forEachPotential() succeeded to run with empty set of potentials.")));
        }

        return ret;
    }

    /**
     * Called after the whole tree has been walked. This method returns the results of any {@link TestResult#DEFERRED}
     * elements after everything has been evaluated.
     *
     * <p>After you also call {@link #reset()}, the matching progress is ready for receiving another round of models.
     *
     * @return the test results for the elements that have previously been {@link TestResult#DEFERRED}.
     */
    public Map<M, TestResult> finish() {
        if (!progressStack.isEmpty()) {
            throw new IllegalStateException("The matching progress is not yet complete. Please finish the progress" +
                    " on all models for which it has been started first (in a depth-first-search order).");
        }

        Map<M, TestResult> ret = allSteps.stream()
                .filter(n -> n.getObject().executionContext.isReturn)
                .flatMap(n -> n.getObject().resolutionCache.entrySet().stream())
                .filter(e -> deferredModels.contains(e.getKey()))
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().decide(false)));

        deferredModels.forEach(m -> {
            if (!ret.containsKey(m)) {
                ret.put(m, NOT_PASSED);
            }
        });

        return ret;
    }

    /**
     * This method can be used to obtain the known test result for any element that has already been analyzed.
     *
     * @param model the model element to test
     * @return the test result
     */
    public TestResult test(M model) {
        TestResult ret = NOT_PASSED;
        for (Node<Step<M>> n : allSteps) {
            Step<M> s = n.getObject();
            if (!s.executionContext.isReturn) {
                continue;
            }

            TestResult sr = s.resolutionCache.get(model);
            if (sr != null) {
                ret = ret.or(sr);
            } else {
                ret = ret.or(DEFERRED);
            }

            if (ret.toBoolean(false)) {
                return ret;
            }
        }

        return ret;
    }

    /**
     * Lose all progress and get ready to start matching models anew.
     */
    public void reset() {
        deferredModels.clear();
        progressStack.clear();
    }

    private List<Node<Step<M>>> determineNextPotentialMatchesFromHierarchy(M model) {
        List<Node<Step<M>>> newPotentials = new ArrayList<>();

        forEachPotential(n -> {
            Step<M> step = n.getObject();
            if (step.independentlyMatchingModels.containsKey(model)) {
                newPotentials.addAll(n.getChildren());
            }
        });

        return newPotentials;
    }

    private TestResult potentialMatchesWithDependencies(M model) {
        return forEachPotential(n -> {
            Step<M> step = n.getObject();

            TestResult result = step.executionContext.match.test(model, step.independentMatchContext);
            if (result.toBoolean(false)) {
                step.independentlyMatchingModels.put(model, null);
            } else {
                return result;
            }

            Map<Node<Step<M>>, List<M>> successRoute = new HashMap<>();

            TestResult res = matchesWithDependencies(n, model, emptyMap(), successRoute)
                    .and(() -> matchesWithDependents(n, model, successRoute));

            step.resolutionCache.compute(model, (__, existing) -> existing == null ? res : existing.or(res));

            if (res.toBoolean(false)) {
                successRoute.forEach((sn, ms) -> ms.forEach(m -> sn.getObject().resolutionCache.put(m, PASSED)));
            }

            return res;
        }, TestResult::or).orElse(PASSED);
    }

    private TestResult matchesWithDependencies(Node<Step<M>> node, M model, Map<Node<Step<M>>, M> boundVariables,
            Map<Node<Step<M>>, List<M>> successRoute) {
        Step<M> nodeStep = node.getObject();

        if (!nodeStep.independentlyMatchingModels.containsKey(model)) {
            nodeStep.resolutionCache.put(model, DEFERRED);
            nodeStep.dependenciesResolutionCache.put(model, DEFERRED);
            return DEFERRED;
        }

        TestResult cumulativeResult = nodeStep.dependenciesResolutionCache.get(model);
        if (cumulativeResult == PASSED) {
            successRoute.computeIfAbsent(node, __ -> new ArrayList<>(8)).add(model);
            return cumulativeResult;
        } else {
            cumulativeResult = DEFERRED;
        }

        // we may only proceed if we have something to match the variables referenced by the current step with
        // we need to try all the possible matches for all the variables

        //get all the combinations of possible matches from the dependency providers
        List<List<NodeAndModel<M>>> allModels = new ArrayList<>();
        for (Node<Step<M>> depProvider : node.in()) {
            Step<M> depStep = depProvider.getObject();
            List<NodeAndModel<M>> models = new ArrayList<>();
            M boundVariable = boundVariables.get(depProvider);
            if (boundVariable != null) {
                models.add(new NodeAndModel<>(depProvider, boundVariable));
            } else {
                for (M depModel : depStep.independentlyMatchingModels.keySet()) {
                    models.add(new NodeAndModel<>(depProvider, depModel));
                }
            }
            allModels.add(models);
        }

        List<List<NodeAndModel<M>>> depCombinations = getCombinations(allModels);
        for (List<NodeAndModel<M>> combination : depCombinations) {
            //check that the current node passes
            MatchContext<M> ctx = nodeStep.blueprintMatchContext;
            for (NodeAndModel<M> nm : combination) {
                ctx = ctx.replace(nm.step.getObject().executionContext.definedVariable, new IsEqual(nm.model));
            }

            TestResult res = nodeStep.executionContext.match.test(model, ctx);

            //check that the dependency providers pass, too
            res = res.and(() ->
                    combination.stream().map(nm -> matchesWithDependencies(nm.step, nm.model, emptyMap(), successRoute))
                            .reduce(TestResult::and).orElse(PASSED));
            cumulativeResult = cumulativeResult.or(res);
        }

        cumulativeResult = node.in().isEmpty() ? PASSED : cumulativeResult;

        nodeStep.dependenciesResolutionCache.put(model, cumulativeResult);

        if (cumulativeResult.toBoolean(false)) {
            successRoute.computeIfAbsent(node, __ -> new ArrayList<>(8)).add(model);
        }

        return cumulativeResult;
    }

    private TestResult matchesWithDependents(Node<Step<M>> node, M model, Map<Node<Step<M>>, List<M>> successRoute) {
        Step<M> nodeStep = node.getObject();

        if (!nodeStep.independentlyMatchingModels.containsKey(model)) {
            nodeStep.resolutionCache.put(model, DEFERRED);
            nodeStep.dependentsResolutionCache.put(model, DEFERRED);
            return DEFERRED;
        }

        TestResult cumulativeResult = nodeStep.dependentsResolutionCache.get(model);
        if (cumulativeResult == PASSED) {
            successRoute.computeIfAbsent(node, __ -> new ArrayList<>(8)).add(model);
            return cumulativeResult;
        } else {
            cumulativeResult = DEFERRED;
        }

        Map<Node<Step<M>>, M> bounds = Collections.singletonMap(node, model);

        for (Node<Step<M>> depNode : node.out()) {
            Step<M> depStep = depNode.getObject();

            for (M depModel : depStep.independentlyMatchingModels.keySet()) {
                // note that we need to process the dependencies and dependents eagerly even if we know the result
                // already. That is so that all the dependents are updated correctly with the result from this node.
                cumulativeResult = cumulativeResult.or(
                        //we're not interested in additional successes that might be added by the dependencies
                        //check of this dependent node. So let's just pass in a throw-away map
                        matchesWithDependencies(depNode, depModel, bounds, new HashMap<>())
                                .and(matchesWithDependents(depNode, depModel, successRoute)));
            }
        }

        cumulativeResult = node.out().isEmpty() ? PASSED : cumulativeResult;

        nodeStep.dependentsResolutionCache.put(model, cumulativeResult);

        if (cumulativeResult.toBoolean(false)) {
            successRoute.computeIfAbsent(node, __ -> new ArrayList<>(8)).add(model);
        }

        return cumulativeResult;
    }

    private void forEachPotential(Consumer<Node<Step<M>>> process) {
        forEachPotential(n -> {
            process.accept(n);
            return Void.class;
        }, (a, b) -> Void.class);
    }

    private <T> Optional<T> forEachPotential(Function<Node<Step<M>>, T> process, BinaryOperator<T> combiner) {
        return (progressStack.isEmpty()
                ? rootMatches.stream()
                : Stream.concat(rootMatches.stream(), requireNonNull(progressStack.peek()).potentialChildMatches.stream())
        ).map(process).reduce(combiner);
    }

    //shamelessly copied from https://stackoverflow.com/a/35652538/1969945
    private static <T> List<List<T>> getCombinations(List<List<T>> lists) {
        if (lists.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> combinations = new ArrayList<>();
        List<List<T>> newCombinations;

        // extract each of the integers in the first list
        // and add each to ints as a new list
        for (T i : lists.get(0)) {
            List<T> newList = new ArrayList<>();
            newList.add(i);
            combinations.add(newList);
        }

        int index = 1;

        while (index < lists.size()) {
            List<T> nextList = lists.get(index);
            newCombinations = new ArrayList<>();

            for (List<T> first : combinations) {
                for (T second : nextList) {
                    List<T> newList = new ArrayList<>(first);
                    newList.add(second);
                    newCombinations.add(newList);
                }
            }

            combinations = newCombinations;

            index++;
        }

        return combinations;
    }

    private static final class Step<M> {
        final MatchExecutionContext executionContext;
        final MatchContext<M> blueprintMatchContext;
        final MatchContext<M> independentMatchContext;

        // this is meant to be an IdentityHashSet, but alas, that doesn't exist out of the box in the standard library
        final IdentityHashMap<M, Void> independentlyMatchingModels = new IdentityHashMap<>();

        final IdentityHashMap<M, TestResult> resolutionCache = new IdentityHashMap<>();

        final IdentityHashMap<M, TestResult> dependenciesResolutionCache = new IdentityHashMap<>();
        final IdentityHashMap<M, TestResult> dependentsResolutionCache = new IdentityHashMap<>();

        boolean thisOrChildrenReturn;
        boolean thisOrChildrenUseVariables;

        private Step(MatchExecutionContext executionContext, MatchContext<M> matchContext,
                MatchContext<M> independentMatchContext) {
            this.executionContext = executionContext;
            this.blueprintMatchContext = matchContext;
            this.independentMatchContext = independentMatchContext;
        }
    }

    private static final class ProgressContext<M> {
        final M model;
        TestResult resolution;
        TestResult childrenResolution = PASSED;
        List<Node<Step<M>>> potentialChildMatches;
        boolean childrenEncountered;

        ProgressContext(M model) {
            this.model = model;
        }
    }

    private static final class NodeAndModel<M> {
        final Node<Step<M>> step;
        final M model;

        private NodeAndModel(Node<Step<M>> step, M model) {
            this.step = step;
            this.model = model;
        }
    }

    private static final class IsEqual extends ModelMatch {
        private final Object model;

        private IsEqual(Object model) {
            this.model = model;
        }

        @Override
        protected <M> TestResult defaultElementTest(M model, MatchContext<M> ctx) {
            //XXX this probably isn't that simple with "real" elements from javac and such...
            return TestResult.fromBoolean(model.equals(this.model));
        }
    }
}
