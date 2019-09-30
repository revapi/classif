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
package org.revapi.classif.match.instance;

import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.util.Glob;
import org.revapi.classif.util.Globbed;
import org.revapi.classif.util.Nullable;

public final class FqnMatch extends TypeInstanceMatch implements Globbed {
    private final boolean matchAny;
    private final boolean matchAll;
    private final @Nullable Glob<NameMatch> glob;

    public FqnMatch(List<NameMatch> names) {
        matchAny = names.size() == 1 && names.get(0).isMatchAny();
        matchAll = names.size() == 1 && names.get(0).isMatchAll();
        if (!matchAny && !matchAll) {
            glob = new Glob<>(names);
        } else {
            glob = null;
        }
    }

    @Override
    public boolean isMatchAny() {
        return matchAny;
    }

    @Override
    public boolean isMatchAll() {
        return matchAll;
    }

    @Override
    public <M> TestResult testAnyInstance(TypeMirror instantiation, MatchContext<M> ctx) {
        // special case - * or ** are considered equal for the fqns...
        if (glob == null) {
            return PASSED;
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
                return NOT_PASSED;
            }
        } else {
            return NOT_PASSED;
        }

        return glob.test((m, n) -> TestResult.fromBoolean(m.matches(n)), split(fqn));
    }

    @Override
    public String toString() {
        return glob == null
                ? (matchAny ? "*" : "**")
                : glob.getMatches().stream().map(NameMatch::toString).collect(Collectors.joining("."));
    }

    private static List<String> split(String fqn) {
        int from = 0;
        int to;

        List<String> ret = new ArrayList<>(8);

        while ((to = fqn.indexOf('.', from)) != -1) {
            ret.add(fqn.substring(from, to));
            from = to + 1;
        }

        if (from == 0) {
            ret.add(fqn);
        } else {
            if (from < fqn.length()) {
                ret.add(fqn.substring(from));
            }
        }


        return ret;
    }

}
