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

public enum Modifier {
    PUBLIC("public") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.PUBLIC);
        }
    },
    PROTECTED("protected") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.PROTECTED);
        }
    },
    PRIVATE("private") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.PRIVATE);
        }
    },
    PACKAGE_PRIVATE("packageprivate") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return !(PUBLIC.matches(mods) || PROTECTED.matches(mods) || PRIVATE.matches(mods));
        }
    },
    ABSTRACT("abstract") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.ABSTRACT);
        }
    },
    DEFAULT("default") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.DEFAULT);
        }
    },
    STATIC("static") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.STATIC);
        }
    },
    FINAL("final") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.FINAL);
        }
    },
    TRANSIENT("transient") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.TRANSIENT);
        }
    },
    VOLATILE("volatile") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.VOLATILE);
        }
    },
    SYNCHRONIZED("synchronized") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.SYNCHRONIZED);
        }
    },
    NATIVE("native") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.NATIVE);
        }
    },
    STRICTFP("strictfp") {
        @Override
        public boolean matches(Set<javax.lang.model.element.Modifier> mods) {
            return mods.contains(javax.lang.model.element.Modifier.STRICTFP);
        }
    };

    private final String name;

    Modifier(String name) {
        this.name = name;
    }

    public abstract boolean matches(Set<javax.lang.model.element.Modifier> mods);

    public static Modifier fromString(String string) {
        for (Modifier m : values()) {
            if (m.name.equals(string)) {
                return m;
            }
        }

        return null;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
