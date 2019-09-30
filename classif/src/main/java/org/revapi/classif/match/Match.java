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
package org.revapi.classif.match;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.declaration.DeclarationMatch;
import org.revapi.classif.match.instance.TypeInstanceMatch;
import org.revapi.classif.progress.context.MatchContext;

public abstract class Match {

    protected Match() {

    }

    public final <M> TestResult test(M model, MatchContext<M> ctx) {
        return test(ctx.getModelInspector().toElement(model), ctx.getModelInspector().toMirror(model), ctx);
    }

    public final <M> TestResult test(Element declaration, TypeMirror instance, MatchContext<M> ctx) {
        if (this instanceof DeclarationMatch) {
            return testDeclaration(declaration, instance, ctx);
        } else if (this instanceof TypeInstanceMatch) {
            return testInstance(instance, ctx);
        } else {
            throw new IllegalStateException("Only DeclarationMatch and TypeInstanceMatch are valid subclasses of the Match class.");
        }
    }

    public abstract <M> TestResult testDeclaration(Element declaration, TypeMirror instance, MatchContext<M> ctx);

    public abstract <M> TestResult testInstance(TypeMirror instance, MatchContext<M> ctx);
}
