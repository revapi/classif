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

import static java.util.Objects.requireNonNull;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.match.MatchContext;

public final class TypeKindMatch extends DeclarationMatch {
    private final boolean negation;
    private final ElementKind kind;

    public TypeKindMatch(boolean negation, String typeKind) {
        this.negation = negation;

        switch (requireNonNull(typeKind)) {
            case "class":
                kind = ElementKind.CLASS;
                break;
            case "interface":
                kind = ElementKind.INTERFACE;
                break;
            case "enum":
                kind = ElementKind.ENUM;
                break;
            case "@interface":
                kind = ElementKind.ANNOTATION_TYPE;
                break;
            case "type":
                kind = ElementKind.OTHER; // meaning "any"
                break;
            default:
                throw new IllegalArgumentException("The kind '" + typeKind + "' of a type not recognized.");
        }
    }

    @Override
    protected <M> boolean testType(TypeElement type, TypeMirror instantiation, MatchContext<M> ctx) {
        boolean matches = kind == ElementKind.OTHER || type.getKind() == kind;

        return negation != matches;
    }
}
