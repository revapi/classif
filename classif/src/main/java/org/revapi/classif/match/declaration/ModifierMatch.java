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

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;

public final class ModifierMatch extends DeclarationMatch {
    private final boolean negation;
    private final Modifier modifier;

    public ModifierMatch(boolean negation, Modifier modifier) {
        this.negation = negation;
        this.modifier = modifier;
    }

    @Override
    protected <M> TestResult defaultTest(Element el, TypeMirror instantiation, MatchContext<M> ctx) {
        Set<javax.lang.model.element.Modifier> modifiers = el.getModifiers();

        boolean ret = modifier.matches(modifiers);
        return TestResult.fromBoolean(negation != ret);
    }

    @Override
    public String toString() {
        return modifier.toString();
    }
}
