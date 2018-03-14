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
public class TestClass {
    public static class Generic<T, E extends T, F extends java.lang.String> {
        public E genericField;

        public T methodT() {
            return null;
        }

        public E methodE() {
            return null;
        }

        public F methodF() {
            return null;
        }

        public <G extends F> G methodG() {
            return null;
        }

        public void genericArgument(F arg) {

        }
    }

    public static class Wildcard {

        public java.util.Collection<? extends java.util.List<?>> methodExtends() {
            return null;
        }

        public java.util.Collection<? super Comparable<?>> methodSuper() {
            return null;
        }

        public java.util.Collection<?> methodWildcard() {
            return null;
        }

        public void wildcardArgument(java.util.Collection<?> arg) {

        }
    }

    public int primitiveField;
    public String referenceField;

    public void voidMethod() {

    }

    public int primitiveMethod() {
        return 0;
    }

    public String referenceMethod() {
        return null;
    }

    public void primitiveArgument(int arg) {

    }

    public void referenceArgument(String arg) {

    }
}
