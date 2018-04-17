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
package org.revapi.classif.match.declaration;

import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.util.Globbed;

public final class AnnotationAttributeMatch implements Globbed {
    private final boolean isAny;
    private final boolean isAll;
    private final NameMatch name;
    private final AnnotationValueMatch valueMatch;

    public AnnotationAttributeMatch(boolean isAny, boolean isAll, NameMatch name,
            AnnotationValueMatch valueMatch) {
        this.isAny = isAny;
        this.isAll = isAll;
        this.name = name;
        this.valueMatch = valueMatch;
    }

    @Override
    public boolean isMatchAny() {
        return isAny || (name != null && valueMatch != null && name.isMatchAny() && valueMatch.isMatchAny());
    }

    @Override
    public boolean isMatchAll() {
        return isAll;
    }

    public <M> boolean test(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> attribute, MatchContext<M> matchContext) {
        return isMatchAny() || isMatchAll()
                || (name.matches(attribute.getKey().getSimpleName().toString())
                && valueMatch.test(attribute.getValue(), matchContext));
    }
}
