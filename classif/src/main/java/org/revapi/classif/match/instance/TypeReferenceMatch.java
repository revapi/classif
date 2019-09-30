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

import static org.revapi.classif.TestResult.TestableStream.testable;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.classif.util.Globbed;

public final class TypeReferenceMatch extends TypeInstanceMatch implements Globbed {
    private final List<SingleTypeReferenceMatch> matches;

    public TypeReferenceMatch(List<SingleTypeReferenceMatch> matches) {
        this.matches = matches;
    }

    @Override
    public boolean isMatchAll() {
        return matches.size() == 1 && matches.get(0).isMatchAll();
    }

    @Override
    public boolean isMatchAny() {
        return matches.size() == 1 && matches.get(0).isMatchAny();
    }

    @Override
    public <M> TestResult testAnyInstance(TypeMirror instantiation, MatchContext<M> ctx) {
        return testable(matches).testAny(m -> m.testInstance(instantiation, ctx));
    }

    @Override
    public String toString() {
        return matches.stream().map(Object::toString).collect(Collectors.joining(" | "));
    }

}
