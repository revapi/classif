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

import static java.util.Collections.emptySet;

import static org.revapi.classif.util.LogUtil.traceParams;
import static org.revapi.classif.util.SizedCollections.newIdentityHashMapWithExactSize;
import static org.revapi.classif.util.SizedCollections.toListWithSize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.EntryMessage;
import org.revapi.classif.ModelInspector;
import org.revapi.classif.StructuralMatcher;
import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.StatementContext;
import org.revapi.classif.util.execution.DependencyGraph;
import org.revapi.classif.util.execution.Node;
import org.revapi.classif.util.execution.StatementWrapper;

/**
 * The progress of the matching of the {@link StructuralMatcher} that created this instance.
 * <p>
 * The {@link #start(Object)} and {@link #finish(Object)} methods are used to instruct the caller on how to execute
 * the depth-first walk of the assumed tree structure of the model elements.
 *
 * @param <M> the type of the representation of the java model elements used by the caller
 */
public abstract class MatchingProgress<M> {
    private static final Logger LOG = LogManager.getLogger(MatchingProgress.class);

    public static <M> MatchingProgress<M> of(DependencyGraph matchGraph, ModelInspector<M> modelInspector,
            StructuralMatcher.Configuration configuration) {
        EntryMessage methodTrace = LOG.traceEntry(traceParams(LOG, "configuration", configuration, "matchGraph",
                matchGraph));

        if (matchGraph.getAllNodes().size() == 1) {
            return LOG.traceExit(methodTrace,
                    new SimpleMatchingProgress<>(configuration, convert(matchGraph.getAllNodes().iterator().next(),
                            modelInspector, new HashMap<>())
                            .getObject()));
        } else {
            IdentityHashMap<Node<StatementWrapper>, Node<StatementMatch<M>>> cache =
                    newIdentityHashMapWithExactSize(matchGraph.getAllNodes().size());

            return LOG.traceExit(methodTrace, new MultiMatchingProgress<>(
                    configuration,
                    matchGraph.getAllNodes().stream()
                            .map(n -> convert(n, modelInspector, cache))
                            .collect(toListWithSize(matchGraph.getAllNodes().size()))));
        }
    }

    MatchingProgress() {
        // package private constructor to prevent subclassing by 3rd parties
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
    public abstract WalkInstruction start(M model);

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
    public abstract TestResult finish(M model);

    /**
     * Called after the whole tree has been walked. This method returns the results of any {@link TestResult#DEFERRED}
     * elements after everything has been evaluated.
     *
     * <p>After you also call {@link #reset()}, the matching progress is ready for receiving another round of models.
     *
     * @return the test results for the elements that have previously been {@link TestResult#DEFERRED}.
     */
    public abstract Map<M, TestResult> finish();

    /**
     * Lose all state and get ready to start matching models anew.
     */
    public abstract void reset();

    private static <M> Node<StatementMatch<M>> convert(Node<StatementWrapper> n, ModelInspector<M> inspector,
            Map<Node<StatementWrapper>, Node<StatementMatch<M>>> cache) {

        Node<StatementMatch<M>> ret = cache.get(n);

        if (ret != null) {
            return ret;
        }

        StatementWrapper wrapper = n.getObject();

        ret = new Node<>(wrapper.getStatement().createMatch());

        cache.put(n, ret);

        Map<Node<StatementWrapper>, Node<StatementMatch<M>>> ins = newIdentityHashMapWithExactSize(n.in().size());
        Map<Node<StatementWrapper>, Node<StatementMatch<M>>> outs = newIdentityHashMapWithExactSize(n.out().size());
        Map<Node<StatementWrapper>, Node<StatementMatch<M>>> children = newIdentityHashMapWithExactSize(n.getChildren().size());

        for (Node<StatementWrapper> in : n.in()) {
            ins.put(in, convert(in, inspector, cache));
        }

        for (Node<StatementWrapper> out : n.out()) {
            outs.put(out, convert(out, inspector, cache));
        }

        for (Node<StatementWrapper> c : n.getChildren()) {
            children.put(c, convert(c, inspector, cache));
        }

        ret.in().addAll(ins.values());
        ret.out().addAll(outs.values());
        ret.getChildren().addAll(children.values());

        int nofReferencedVariables = n.in().size();

        Set<String> referencedStatements = nofReferencedVariables == 0
                ? emptySet()
                : new HashSet<>(nofReferencedVariables);

        for (String v : n.getObject().getStatement().getReferencedVariables()) {
            // just a consistency check
            StatementMatch<M> referencedMatch = n.in().stream()
                    .filter(o -> v.equals(o.getObject().getStatement().getDefinedVariable()))
                    .map(ins::get)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Invalid dependency graph. Could not find a node defining variable "
                            + v)).getObject();

            referencedStatements.add(v);
        }

        ret.getObject().setContext(new StatementContext<>(inspector, wrapper.isReturn(),
                wrapper.getStatement().getDefinedVariable(), referencedStatements));
        return ret;
    }
}
