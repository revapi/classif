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

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.util.Nullable;

public final class DefaultValueMatch extends DeclarationMatch {

    private final @Nullable AnnotationValueMatch valueMatch;

    public DefaultValueMatch(@Nullable AnnotationValueMatch valueMatch) {
        this.valueMatch = valueMatch;
    }

    @Override
    protected <M> TestResult testMethod(ExecutableElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        AnnotationValue defaultValue = declaration.getDefaultValue();

        if (defaultValue == null) {
            return TestResult.fromBoolean(valueMatch == null);
        }

        return valueMatch == null
                ? TestResult.NOT_PASSED
                : valueMatch.test(defaultValue, ctx);
    }
}
