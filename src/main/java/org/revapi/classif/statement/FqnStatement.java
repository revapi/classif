/*
 * Copyright 2014-2018 Lukas Krejci
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
package org.revapi.classif.statement;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.QualifiedNameable;

import org.revapi.classif.ModelInspector;

public final class FqnStatement extends AbstractStatement {
    private final Pattern fqnRegex;

    public FqnStatement(List<NameStatement> names) {
        super(null, emptyList(), false);
        this.fqnRegex = toPattern(names);
    }

    @Override
    public AbstractMatcher createMatcher() {
        return new AbstractMatcher() {
            @Override
            public <E> boolean defaultElementTest(E element, ModelInspector<E> inspector,
                    Map<String, AbstractMatcher> variables) {
                if (fqnRegex == null) {
                    // special case - to save us from needlessly compare the fqn when we match everything anyway, the
                    // regex is null in this case
                    return true;
                }

                Element el = inspector.toElement(element);

                if (!(el instanceof QualifiedNameable)) {
                    return false;
                }

                QualifiedNameable q = (QualifiedNameable) el;

                String fqn = q.getQualifiedName().toString();

                return fqnRegex.matcher(fqn).matches();
            }
        };
    }

    private static Pattern toPattern(List<NameStatement> names) {
        // special case - a lone single star means "everything"
        if (names.size() == 1 && (names.get(0).isMatchAny() || names.get(0).isMatchAllRemaining())) {
            return null;
        }

        StringBuilder sb = new StringBuilder("^");

        for (NameStatement st : names) {
            if (st.isMatchAny()) {
                sb.append("[^.]+");
            } else if (st.isMatchAllRemaining()) {
                sb.append(".+");
            } else if (st.getExactMatch() != null) {
                sb.append(Pattern.quote(st.getExactMatch()));
            } else if (st.getPattern() != null) {
                sb.append(st.getPattern().pattern());
            }
            sb.append("\\.");
        }

        if (sb.length() > 1) {
            sb.replace(sb.length() - 2, sb.length(), "$");
        }

        return Pattern.compile(sb.toString());
    }
}
