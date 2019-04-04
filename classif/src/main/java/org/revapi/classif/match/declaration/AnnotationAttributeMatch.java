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
package org.revapi.classif.match.declaration;

import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.util.Globbed;
import org.revapi.classif.util.Nullable;

public final class AnnotationAttributeMatch implements Globbed {
    private final boolean isAny;
    private final boolean isAll;
    private final @Nullable NameMatch name;
    private final @Nullable AnnotationValueMatch valueMatch;

    public AnnotationAttributeMatch(boolean isAny, boolean isAll, @Nullable NameMatch name,
            @Nullable AnnotationValueMatch valueMatch) {
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

    public <M> TestResult test(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> attribute,
            MatchContext<M> matchContext) {
        return TestResult.fromBoolean(isMatchAny() || isMatchAll()
                || (name == null || name.matches(attribute.getKey().getSimpleName().toString())))
                .and(() -> {
                    if (valueMatch == null) {
                        return TestResult.PASSED;
                    } else {
                        return valueMatch.test(attribute.getKey(), attribute.getValue(), matchContext);
                    }
                });
    }

    @Override
    public String toString() {
        if (isAny) {
            return "*";
        }

        if (isAll) {
            return "**";
        }

        return "" + name + " " + valueMatch;
    }
}
