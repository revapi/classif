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

import javax.lang.model.type.TypeMirror;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.util.Globbed;

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
    public <M> boolean testInstance(TypeMirror instantiation, MatchContext<M> ctx) {
        return matches.stream().anyMatch(m -> m.testInstance(instantiation, ctx));
    }
}
