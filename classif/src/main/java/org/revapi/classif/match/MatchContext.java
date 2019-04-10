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
package org.revapi.classif.match;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import org.revapi.classif.ModelInspector;
import org.revapi.classif.statement.StatementMatch;

public final class MatchContext<M> {

    public final ModelInspector<M> modelInspector;
    public final Map<String, StatementMatch> variables;

    public MatchContext(ModelInspector<M> modelInspector, Map<String, StatementMatch> variables) {
        this.modelInspector = requireNonNull(modelInspector);
        this.variables = unmodifiableMap(requireNonNull(variables));
    }

    public MatchContext<M> replace(String variable, StatementMatch statementMatch) {
        Map<String, StatementMatch> newVars = new HashMap<>(variables);
        newVars.put(variable, statementMatch);
        return new MatchContext<>(modelInspector, newVars);
    }

    @Override
    public String toString() {
        return "MatchContext{" +
                "modelInspector=" + modelInspector +
                ", variables=" + variables +
                '}';
    }
}
