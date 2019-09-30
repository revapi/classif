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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.revapi.classif.StructuralMatcher;
import org.revapi.classif.TestResult;

final class SimpleMatchingProgress<M> extends MatchingProgress<M> {
    private static final Logger LOG = LogManager.getLogger(SimpleMatchingProgress.class);

    private final StatementMatch<M> statementMatch;
    private final StructuralMatcher.Configuration configuration;
    private final Map<M, TestResult> activeTestResults = new IdentityHashMap<>();

    SimpleMatchingProgress(StructuralMatcher.Configuration configuration, StatementMatch<M> statementMatch) {
        this.configuration = configuration;
        this.statementMatch = statementMatch;
    }

    @Override
    public WalkInstruction start(M model) {
        TestResult res = statementMatch.test(model, statementMatch.getContext().getMatchContext());
        activeTestResults.put(model, res);
        return LOG.traceExit(WalkInstruction.of(!configuration.isStrictHierarchy(), res));
    }

    @Override
    public TestResult finish(M model) {
        TestResult res = activeTestResults.remove(model);
        if (res == null) {
            throw new IllegalArgumentException("Unbalanced start/finish call.");
        } else {
            return res;
        }
    }

    @Override
    public Map<M, TestResult> finish() {
        return Collections.emptyMap();
    }

    @Override
    public void reset() {
        activeTestResults.clear();
        statementMatch.reset();
    }
}
