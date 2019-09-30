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

import static org.revapi.classif.TestResult.PASSED;
import static org.revapi.classif.util.LogUtil.traceParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor8;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.EntryMessage;
import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.classif.progress.context.StatementContext;
import org.revapi.classif.util.Nullable;

public abstract class StatementMatch<M> {
    private static final Logger LOG = LogManager.getLogger(StatementMatch.class);

    // never access these directly - always use the property accessors
    private StatementContext<M> ctx;
    private Map<M, TestResult> matchCandidates = new HashMap<>();

    protected StatementContext<M> getContext() {
        return ctx;
    }

    public void setContext(StatementContext<M> ctx) {
        this.ctx = ctx;
    }

    public void reset() {
        matchCandidates = new HashMap<>();
    }

    public final TestResult test(M model, MatchContext<M> ctx) {
        return forwardTest(model, ctx, tester(model, ctx));
    }

    public TestResult independentTest(M model) {
        EntryMessage methodTrace = LOG.traceEntry(traceParams(LOG, "this", this, "model", model));

        MatchContext<M> ctx = getContext().getMatchContext();
        TestResult ret = forwardTest(model, ctx, tester(model, ctx));
        if (ret == PASSED) {
            matchCandidates.put(model, ret);
        }

        return LOG.traceExit(methodTrace, ret);
    }

    public Set<M> getCandidates() {
        return matchCandidates.keySet();
    }

    @Override
    public String toString() {
        return "StatementMatch{" +
                "ctx=" + ctx +
                '}';
    }

    protected TestResult testType(M type, MatchContext<M> ctx) {
        return defaultElementTest(type, ctx);
    }

    protected TestResult testMethod(M method, MatchContext<M> ctx) {
        return defaultElementTest(method, ctx);
    }

    protected TestResult testVariable(M var, MatchContext<M> ctx) {
        return defaultElementTest(var, ctx);
    }

    protected TestResult defaultElementTest(M model, MatchContext<M> ctx) {
        return TestResult.NOT_PASSED;
    }

    private @Nullable TestResult forwardTest(M model, MatchContext<M> ctx,
            ElementVisitor<TestResult, MatchContext<M>> tester) {
        EntryMessage methodTrace = LOG.traceEntry(traceParams(LOG, "this", this, "model", model, "ctx", ctx));
        return LOG.traceExit(methodTrace, tester.visit(ctx.getModelInspector().toElement(model),
                ctx));
    }

    private ElementVisitor<TestResult, MatchContext<M>> tester(M model, MatchContext<M> ctx) {
        return new SimpleElementVisitor8<TestResult, MatchContext<M>>() {
            @Override
            protected TestResult defaultAction(Element e, MatchContext<M> ctx) {
                return TestResult.NOT_PASSED;
            }

            @Override
            public TestResult visitVariable(VariableElement e, MatchContext<M> ctx) {
                return testVariable(model, ctx);
            }

            @Override
            public TestResult visitType(TypeElement e, MatchContext<M> ctx) {
                return testType(model, ctx);
            }

            @Override
            public TestResult visitExecutable(ExecutableElement e, MatchContext<M> ctx) {
                return testMethod(model, ctx);
            }
        };
    }
}
