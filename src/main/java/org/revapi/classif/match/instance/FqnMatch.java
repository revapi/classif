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
package org.revapi.classif.match.instance;

import java.util.List;
import java.util.regex.Pattern;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.NameMatch;

public final class FqnMatch extends TypeInstanceMatch {
    private final Pattern fqnRegex;

    public FqnMatch(List<NameMatch> names) {
        this.fqnRegex = toPattern(names);
    }

    @Override
    protected <M> boolean testInstance(TypeMirror instantiation, MatchContext<M> ctx) {
        if (fqnRegex == null) {
            // special case - to save us from needlessly compare the fqn when we match everything anyway, the
            // regex is null in this case
            return true;
        }

        String fqn;
        if (instantiation instanceof DeclaredType) {
            fqn = ((TypeElement) ((DeclaredType) instantiation).asElement()).getQualifiedName().toString();
        } else if (instantiation instanceof PrimitiveType) {
            fqn = instantiation.toString();
        } else if (instantiation instanceof NoType) {
            if (instantiation.getKind() == TypeKind.VOID) {
                fqn = "void";
            } else {
                return false;
            }
        } else {
            return false;
        }

        return fqnRegex.matcher(fqn).matches();
    }

    private static Pattern toPattern(List<NameMatch> names) {
        // special case - a lone single star means "everything"
        if (names.size() == 1 && (names.get(0).isMatchAny() || names.get(0).isMatchAllRemaining())) {
            return null;
        }

        StringBuilder sb = new StringBuilder("^");

        for (NameMatch st : names) {
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
