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

import javax.lang.model.element.ElementKind;

public enum TypeKind {
    CLASS("class") {
        @Override
        public boolean matches(ElementKind elementKind) {
            return elementKind == ElementKind.CLASS;
        }
    }, INTERFACE("interface") {
        @Override
        public boolean matches(ElementKind elementKind) {
            return elementKind == ElementKind.INTERFACE;
        }
    }, ENUM("enum") {
        @Override
        public boolean matches(ElementKind elementKind) {
            return elementKind == ElementKind.ENUM;
        }
    }, ANNOTATION_TYPE("@interface") {
        @Override
        public boolean matches(ElementKind elementKind) {
            return elementKind == ElementKind.ANNOTATION_TYPE;
        }
    }, ANY("type") {
        @Override
        public boolean matches(ElementKind elementKind) {
            return true;
        }
    };

    private final String string;

    TypeKind(String string) {
        this.string = string;
    }

    public static TypeKind fromString(String string) {
        for (TypeKind t : TypeKind.values()) {
            if (t.string.equals(string)) {
                return t;
            }
        }

        return null;
    }

    public abstract boolean matches(ElementKind elementKind);

    public String toString() {
        return string;
    }
}
