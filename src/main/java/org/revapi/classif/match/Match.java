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
package org.revapi.classif.match;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.match.declaration.DeclarationMatch;
import org.revapi.classif.match.instance.TypeInstanceMatch;

public abstract class Match {

    protected Match() {

    }

    public final <M> boolean test(M model, MatchContext<M> ctx) {
        return test(ctx.modelInspector.toElement(model), ctx.modelInspector.toMirror(model), ctx);
    }

    public final <M> boolean test(Element declaration, TypeMirror instance, MatchContext<M> ctx) {
        if (this instanceof DeclarationMatch) {
            return testDeclaration(declaration, instance, ctx);
        } else if (this instanceof TypeInstanceMatch) {
            return testInstance(instance, ctx);
        } else {
            throw new IllegalStateException("Only DeclarationMatch and TypeInstanceMatch are valid subclasses of the Match class.");
        }
    }

    protected abstract <M> boolean testDeclaration(Element declaration, TypeMirror instance, MatchContext<M> ctx);

    protected abstract <M> boolean testInstance(TypeMirror instance, MatchContext<M> ctx);
}
