package org.revapi.classif;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.util.execution.DependencyGraph;
import org.revapi.classif.util.execution.MatchExecutionContext;
import org.revapi.classif.util.execution.Node;

public final class MatchingProgress<M> {
    private static final ModelMatch MATCH_ANY = new ModelMatch() {
        @Override
        protected <MM> boolean defaultElementTest(MM model, MatchContext<MM> ctx) {
            return true;
        }
    };

    private final List<Node<Step<M>>> allSteps;
    private final List<List<Node<Step<M>>>> activeTrails;

    MatchingProgress(DependencyGraph matchGraph, ModelInspector<M> inspector) {
        IdentityHashMap<Node<MatchExecutionContext>, Node<Step<M>>> cache =
                new IdentityHashMap<>(matchGraph.getAllNodes().size());

        allSteps = matchGraph.getAllNodes().stream()
                .map(n -> convert(n, inspector, cache)).collect(toList());

        //we just need the roots
        List<Node<Step<M>>> roots = allSteps.stream().filter(n -> n.in().isEmpty()).collect(toList());

        //bootstrap the active trails with the roots of the graph
        this.activeTrails = new ArrayList<>(roots.size());
        for (Node<Step<M>> n : roots) {
            List<Node<Step<M>>> trail = new ArrayList<>();
            trail.add(n);
            activeTrails.add(trail);
        }
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
            for (Node<MatchExecutionContext> o : n.out()) {
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
            convert(in, inspector, cache);
        }

        for (Node<MatchExecutionContext> out : n.out()) {
            convert(out, inspector, cache);
        }

        return ret;
    }

    public TestResult test(M model) {
        for (Node<Step<M>> n : allSteps) {
            Step<M> step = n.getObject();

            if (step.executionContext.match.test(model, step.independentMatchContext)) {
                step.independentlyMatchingModels.put(model, null);
            }
        }

        //TODO implement
        return TestResult.DEFERRED;
    }

    public Map<M, TestResult> finish() {
        // TODO implement
        activeTrails.clear();
        return Collections.emptyMap();
    }

    private static final class Step<M> {
        final MatchExecutionContext executionContext;
        final MatchContext<M> matchContext;
        final MatchContext<M> independentMatchContext;

        // this is meant to be an IdentityHashSet, but alas, that doesn't exist out of the box
        final IdentityHashMap<M, Void> independentlyMatchingModels = new IdentityHashMap<>();

        private Step(MatchExecutionContext executionContext, MatchContext<M> matchContext,
                MatchContext<M> independentMatchContext) {
            this.executionContext = executionContext;
            this.matchContext = matchContext;
            this.independentMatchContext = independentMatchContext;
        }
    }
}
