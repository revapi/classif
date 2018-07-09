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

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;

public final class ModifierMatch extends DeclarationMatch {
    private final boolean negation;
    private final Modifier modifier;
    private final boolean packagePrivate;

    public ModifierMatch(boolean negation, String modifier) {
        this.negation = negation;
        switch (modifier) {
            case "public":
                this.modifier = Modifier.PUBLIC;
                break;
            case "private":
                this.modifier = Modifier.PRIVATE;
                break;
            case "packageprivate":
                // handled below
                this.modifier = null;
                break;
            case "protected":
                this.modifier = Modifier.PROTECTED;
                break;
            case "static":
                this.modifier = Modifier.STATIC;
                break;
            case "final":
                this.modifier = Modifier.FINAL;
                break;
            case "abstract":
                this.modifier = Modifier.ABSTRACT;
                break;
            case "volatile":
                this.modifier = Modifier.VOLATILE;
                break;
            case "strictfp":
                this.modifier = Modifier.STRICTFP;
                break;
            default:
                throw new IllegalArgumentException("Unsupported modifier: " + modifier);
        }

        this.packagePrivate = "packageprivate".equals(modifier);
    }

    @Override
    protected <M> TestResult defaultTest(Element el, TypeMirror instantiation, MatchContext<M> ctx) {
        Set<Modifier> modifiers = el.getModifiers();

        boolean ret;
        if (packagePrivate) {
            ret = !(modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED)
                    || modifiers.contains(Modifier.PRIVATE));
        } else {
            ret = modifiers.contains(modifier);
        }

        return TestResult.fromBoolean(negation != ret);
    }
}
