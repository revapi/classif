package org.revapi.classif;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
import org.revapi.classif.util.execution.MatchExecution;
import org.revapi.classif.util.execution.MatchExecutionContext;
import org.revapi.classif.util.execution.Node;

public final class MatchingProgress<M> {
    private final List<Node<Step<M>>> steps;

    private final List<Node<Step<M>>> possibleNextSteps;

    public MatchingProgress(MatchExecution execution, ModelInspector<M> inspector) {
        IdentityHashMap<Node<MatchExecutionContext>, Node<Step<M>>> cache =
                new IdentityHashMap<>(execution.getMatches().size());

        List<Node<Step<M>>> allSteps = execution.getMatches().stream()
                .map(n -> convert(n, inspector, cache)).collect(toList());

        //we just need the roots
        this.steps = allSteps.stream().filter(n -> n.in().isEmpty()).collect(toList());
        this.possibleNextSteps = new ArrayList<>(this.steps);
    }

    private static <M> Node<Step<M>> convert(Node<MatchExecutionContext> n, ModelInspector<M> inspector,
            Map<Node<MatchExecutionContext>, Node<Step<M>>> cache) {

        Node<Step<M>> ret = cache.get(n);

        if (ret != null) {
            return ret;
        }

        Map<String, ModelMatch> situationalMatches = new HashMap<>(n.getObject().referencedVariables.size());
        for (String v : n.getObject().referencedVariables) {
            ModelMatch variableMatch = null;
            for (Node<MatchExecutionContext> o : n.out()) {
                if (v.equals(o.getObject().definedVariable)) {
                    variableMatch = o.getObject().match;
                }
            }

            if (variableMatch == null) {
                //match everything if there is no explicit matcher found
                variableMatch = new ModelMatch() {
                    @Override
                    protected <MM> boolean defaultElementTest(MM model, MatchContext<MM> ctx) {
                        return true;
                    }
                };
            }

            situationalMatches.put(v, variableMatch);
        }

        ret = new Node<>(new Step<>(n.getObject(), new MatchContext<>(inspector, situationalMatches)));
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
        //TODO implement
        return TestResult.DEFERRED;
    }

    public Map<M, TestResult> finish() {
        // TODO implement
        return Collections.emptyMap();
    }

    private static final class Step<M> {
        final MatchExecutionContext executionContext;
        final MatchContext<M> matchContext;

        private Step(MatchExecutionContext executionContext, MatchContext<M> matchContext) {
            this.executionContext = executionContext;
            this.matchContext = matchContext;
        }
    }
}
