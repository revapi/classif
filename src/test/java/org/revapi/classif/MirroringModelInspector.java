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
package org.revapi.classif;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class MirroringModelInspector implements ModelInspector<Element> {
    private final Elements elements;
    private final Types types;

    public MirroringModelInspector(Elements elements, Types types) {
        this.elements = elements;
        this.types = types;
    }

    @Override
    public TypeElement getJavaLangObjectElement() {
        return elements.getTypeElement("java.lang.Object");
    }

    @Override
    public Element toElement(Element model) {
        return model;
    }

    @Override
    public TypeMirror toMirror(Element model) {
        return model.asType();
    }

    @Override
    public Set<Element> getUses(Element model) {
        return Collections.emptySet();
    }

    @Override
    public Set<Element> getUseSites(Element model) {
        return Collections.emptySet();
    }

    @Override
    public Element fromElement(Element element) {
        return element;
    }

    @Override
    public List<? extends TypeMirror> directSupertypes(TypeMirror type) {
        return types.directSupertypes(type);
    }

    @Override
    public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
        return elements.overrides(overrider, overridden, type);
    }
}
