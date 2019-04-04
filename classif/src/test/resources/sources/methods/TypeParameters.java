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
public class TypeParameters<T, U extends String> {

    public U useClassTypeParamsMethod(T arg) {
        return null;
    }

    public <X, Y extends Number> X ownTypeParamsMethod(Y arg) {
        return null;
    }

    public void bareWildcardMethod(java.util.Set<?> arg) {

    }

    public void extendingWildcardMethod(java.util.Set<? extends String> arg) {

    }

    public void superWildcardMethod(java.util.Set<? super Comparable<?>> arg) {

    }

    public void classTypeParamsBasedWildcard(java.util.Set<? extends U> arg) {

    }

    public <X extends Number> void ownTypeParamBasedWildcard(java.util.Set<X> arg) {

    }
}
