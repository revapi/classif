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

import org.revapi.classif.progress.context.StatementContext;
import org.revapi.classif.statement.AbstractStatement;
import org.revapi.classif.progress.StatementMatch;

/**
 * A thin wrapper around the {@link AbstractStatement} that caches the {@link StatementMatch} created from it and can
 * override the {@link AbstractStatement#isMatch()} by its {@link #isReturn()}.
 *
 * <p>The nodes in the {@link DependencyGraph} hold these contexts. As such a single execution context represents one
 * statement in the structural match.
 */
public final class StatementWrapper {
    private final boolean isReturn;

    private StatementMatch match;

    private final AbstractStatement statement;

    // package private so that this is not completely free for reuse.
    StatementWrapper(AbstractStatement statement, boolean isReturn) {
        this.isReturn = isReturn;
        this.statement = statement;
    }

    @Override
    public String toString() {
        return (isReturn() ? "^" : "") + getStatement().createMatch().toString();
    }

    /**
     * Whether or not this context represents a statement used as a return from the structural match
     */
    public boolean isReturn() {
        return isReturn;
    }

    /**
     * The match object actually performing the matching operation on the model data
     *
     * @deprecated use {@link #getStatement()}.{@link AbstractStatement#createMatch(StatementContext) createMatch(MatchContext)}
     */
    @Deprecated
    public StatementMatch getMatch() {
        if (match == null) {
            match = statement.createMatch();
        }
        return match;
    }

    /**
     * The original statement this execution context represents.
     */
    public AbstractStatement getStatement() {
        return statement;
    }
}
